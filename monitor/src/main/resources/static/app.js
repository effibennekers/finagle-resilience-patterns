var stompClient = null;

var events = [];
var windowSize = 10;

var taskRunnerId;

var instances = [];
//     {
//         name: 'effi',
//         port: 8081,
//         tps: 0
//     },
//     {
//         name: 'eggie',
//         port: 8082,
//         tps: 0
//     }
// ];

var instanceTable = {};


$(document).ready(function () {
    $('input[id="windowSize"]').val(windowSize);
    $("#apachestoploadbalancing").prop("disabled", true);
    $("#finaglestoploadbalancing").prop("disabled", true);
    $("#numberOfThreads").val(10);

    $.get("/instances", function (data) {
        instances = data;
        instanceTable = {};
        instances.forEach(function (instance, index) {
            instanceTable[instance.name] = instance;
            addInstanceToHtml(instance, index);
            $.get("http://localhost:" + instance.port + "/simulation", function (data) {
                updateSimulation(index, data);
            }, "json");
        });

    }, "json");


    connect();
    refreshDisplay();
});

function updateSimulation(index, data) {
    $('input[id="SimulationBasetime_' + index + '"]').val(data.baseTime);
    $('input[id="SimulationRandom_' + index + '"]').val(data.random);
    $('input[id="SimulationRandomMultiplier_' + index + '"]').val(data.randomMultiplier);
}

function addInstanceToHtml(instance, index) {
    $("#instances-container").append(
        "<div class='left'>\
            <div> \
            <label class='header'>" + instance.name + "</label>\
        </div> \
        <div> \
        <form class='form-inline configuration'> \
            <label for='SimulationBasetime_" + index + "'>base time</label> \
            <input type='number' id='SimulationBasetime_" + index + "'> \
            <label for='SimulationRandom_" + index + "'>random</label> \
            <input type='number' id='SimulationRandom_" + index + "'> \
            <label for='SimulationRandomMultiplier_" + index + "'>random multiplier</label> \
            <input type='number' id='SimulationRandomMultiplier_" + index + "'> \
            <button id='Settings_" + index + "' class='btn btn-default'>update settings</button> \
        </form>\
        </div>\
    </div>\
    <div class='left'>\
        <div>TPM</div>\
        <div id='tps_" + index + "'></div>\
        </div>\
        <div class='left'>\
        <div>Succes/Failure ratio</div>\
            <div id='Ratio_" + index + "'></div>\
        </div>\
        <div class='left'>\
        <div>ok</div>\
        <div class='metrics'>\
        <div id='successMetrics_" + index + "'></div>\
        </div>\
        </div>\
        <div class='left'>\
        <div>failures</div>\
        <div class='metrics'>\
        <div id='failureMetrics_" + index + "'></div>\
        </div>\
        </div>");

    $("#Settings_" + index).click(function () {
        $.post({
            crossOrigin: true,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            'type': 'POST',
            'url': 'http://localhost:' + instance.port + '/simulation',
            'data': JSON.stringify({
                baseTime: $('input[id="SimulationBasetime_' + index + '"]').val(),
                random: $('input[id="SimulationRandom_' + index + '"]').val(),
                randomMultiplier: $('input[id="SimulationRandomMultiplier_' + index + '"]').val()
            }),
            'dataType': 'json'

        }, function (data) {
            updateSimulation(index, data);
        });
        return false;
    });
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/loadbalancing', function (response) {
            var event = JSON.parse(response.body);
            var instance = instanceTable[event.instance];
            if (instance) {
                events.push(event);
                instance.tps++;
            }
            else {
                console.log("ignoring " + JSON.stringify(event));
            }
        });
    });
}

$(function () {
    setInterval(refreshDisplay, 1000);
});

function refreshDisplay() {
    var eventsToProcess = events.slice(-windowSize);

    var counterTable = {};

    for (var key in instanceTable) {
        counterTable[key] = {
            successDuration: 0,
            failureDuration: 0,
            successCounter: 0,
            failureCounter: 0
        };
    }

    instances.forEach(function (instance, index) {
        $("#tps_" + index).text(instance.tps);
        instance.tps = 0;
    });
    eventsToProcess.forEach(function (e) {
        var counter = counterTable[e.instance];
        if (counter) {
            if (e.httpStatus == 200) {
                counter.successDuration += e.duration;
                counter.successCounter++;
            }
            else {
                counter.failureDuration += e.duration;
                counter.failureCounter++;
            }
        }
        else {
            console.log("unable to process event " + JSON.stringify(e));
        }
    });


    var totalCounter = 0;
    var successCounter = 0;
    instances.forEach(function (instance, index) {
        var counter = counterTable[instance.name];
        successCounter += counter.successCounter;
        if (counter.successCounter > 0) {
            $("#successMetrics_" + index).text('count: ' + counter.successCounter + ', average: ' + Math.round(counter.successDuration / counter.successCounter));
        }
        else {
            $("#successMetrics_" + index).text('count: ' + counter.successCounter + ', average: -');
        }
        if (counter.failureCounter > 0) {
            $("#failureMetrics_" + index).text('count: ' + counter.failureCounter + ', average: ' + Math.round(counter.failureDuration / counter.failureCounter));
        }
        else {
            $("#failureMetrics_" + index).text('count: ' + counter.failureCounter + ', average: -');
        }
        var total = counter.successCounter + counter.failureCounter;
        totalCounter += total;
        if (total > 0) {
            $("#Ratio_" + index).text(Math.round((counter.successCounter * 10000) / total) / 100);
        }
        else {
            $("#Ratio_" + index).text("-");
        }

    });

    if (totalCounter > 0) {
        $("#overallRatio").text(Math.round((successCounter * 10000) / totalCounter) / 100);
    }
    else {
        $("#overallRatio").text("-");
    }

}

function startLoadbalancing(taskrunner) {
    $("#finaglestartloadbalancing").prop("disabled", true);
    $("#apachestartloadbalancing").prop("disabled", true);
    $("#" + taskrunner + "stoploadbalancing").prop("disabled", false);

    $.post({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'PUT',
        'url': 'runner/' + taskrunner + '/loadbalancing',
        'data': JSON.stringify({'numberOfThreads': $("#numberOfThreads").val()}),
        'dataType': 'json'
    }, function (data) {
        taskRunnerId = data;
    });
}

function stopLoadbalancing() {
    $("#apachestartloadbalancing").prop("disabled", false);
    $("#finaglestartloadbalancing").prop("disabled", false);
    $("#apachestoploadbalancing").prop("disabled", true);
    $("#finaglestoploadbalancing").prop("disabled", true);
    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'DELETE',
        'url': '/runner/' + taskRunnerId
    });
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#apachestartloadbalancing").click(function () {
        startLoadbalancing("apache");
    });
    $("#apachestoploadbalancing").click(function () {
        stopLoadbalancing();
    });
    $("#finaglestartloadbalancing").click(function () {
        startLoadbalancing("finagle");
    });
    $("#finaglestoploadbalancing").click(function () {
        stopLoadbalancing();
    });
    $("#reset").click(function () {
        events = [];
        refreshDisplay();
    });
    $("#refreshWindowSize").click(function () {
        windowSize = $("#windowSize").val();
    });
});
