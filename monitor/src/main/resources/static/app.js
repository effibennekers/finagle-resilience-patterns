var stompClient = null;


var events = [];
var windowSize = 10;
var effiTps = 0;
var eggieTps = 0;
var effiSimulation = {};
var eggieSimulation = {};

var taskRunnerId;

function updateEffiSimulation() {
    $('input[id="effiSimulationBasetime"]').val(effiSimulation.baseTime);
    $('input[id="effiSimulationRandom"]').val(effiSimulation.random);
    $('input[id="effiSimulationRandomMultiplier"]').val(effiSimulation.randomMultiplier);
}

function updateEggieSimulation() {
    $('input[id="eggieSimulationBasetime"]').val(eggieSimulation.baseTime);
    $('input[id="eggieSimulationRandom"]').val(eggieSimulation.random);
    $('input[id="eggieSimulationRandomMultiplier"]').val(eggieSimulation.randomMultiplier);
}

$(document).ready(function () {
    $('input[id="windowSize"]').val(windowSize);
    $.get("http://localhost:8081/simulation", function (data) {
        effiSimulation = data;
        updateEffiSimulation();
    }, "json");

    $.get("http://localhost:8082/simulation", function (data) {
        eggieSimulation = data;
        updateEggieSimulation();
    }, "json");
    $("#stoploadbalancing").prop("disabled", true);
});

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/loadbalancing', function (response) {
            var event = JSON.parse(response.body);
            events.push(event);
            switch (event.instance) {
                case 'effi':
                    effiTps++;
                    break;
                case 'eggie':
                    eggieTps++;
                    break;
                default:
                    console.log("ignoring " + JSON.stringify(event));

            }
        });
    });
};

connect();
refreshDisplay();


$(function () {
    setInterval(refreshDisplay, 1000);
});


function refreshDisplay() {
    var eventsToProcess = events.slice(-windowSize);

    var effiCounter = 0;
    var eggieCounter = 0;
    var effiTotal = 0;
    var eggieTotal = 0;

    $("#effiTps").text(effiTps);
    $("#eggieTps").text(eggieTps);
    effiTps = 0;
    eggieTps = 0;
    eventsToProcess.forEach(function (e) {
        switch (e.instance) {
            case 'effi':
                effiCounter++;
                effiTotal += e.duration;
                break;

            case 'eggie':
                eggieCounter++;
                eggieTotal += e.duration;
                break;
            default:
                console.log("unable to process event " + JSON.stringify(e));
        }

    });

    if (effiCounter > 0) {
        $("#effi").text('count: ' + effiCounter + ', average: ' + Math.round(effiTotal / effiCounter));
    }
    else {
        $("#effi").text('count: ' + effiCounter + ', average: -');
    }

    if (eggieCounter > 0) {
        $("#eggie").text('count: ' + eggieCounter + ', average: ' + +Math.round(eggieTotal / eggieCounter));
    }
    else {
        $("#eggie").text('count: ' + eggieCounter + ', average: -');
    }
}





function startLoadbalancing() {
    $("#startloadbalancing").prop("disabled", true);
    $("#stoploadbalancing").prop("disabled", false);

    $.post({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'POST',
        'url': '/loadbalancing',
        'data': JSON.stringify({'numberOfThreads': $("#numberOfThreads").val()}),
        'dataType': 'json'
    }, function (data) {
        taskRunnerId = data;
        console.log("task runner id: " + taskRunnerId);

    });
}

function stopLoadbalancing() {
    $("#startloadbalancing").prop("disabled", false);
    $("#stoploadbalancing").prop("disabled", true);
    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'DELETE',
        'url': '/loadbalancing/' + taskRunnerId
    });
}
console.log('start disabled');

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#startloadbalancing").click(function () {
        $(this).attr("disabled", "disabled");
        startLoadbalancing();
    });
    $("#stoploadbalancing").click(function () {
        stopLoadbalancing();
    });
    $("#effiSettings").click(function () {
        $.post({
            crossOrigin: true,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            'type': 'POST',
            'url': 'http://localhost:8081/simulation',
            'data': JSON.stringify({
                baseTime: $('input[id="effiSimulationBasetime"]').val(),
                random: $('input[id="effiSimulationRandom"]').val(),
                randomMultiplier: $('input[id="effiSimulationRandomMultiplier"]').val(),
            }),
            'dataType': 'json'

        }, function (data) {
            effiSimulation = data;
            updateEffiSimulation();
        });
    });
    $("#eggieSettings").click(function () {
        $.post({
            crossOrigin: true,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            'type': 'POST',
            'url': 'http://localhost:8082/simulation',
            'data': JSON.stringify({
                baseTime: $('input[id="eggieSimulationBasetime"]').val(),
                random: $('input[id="eggieSimulationRandom"]').val(),
                randomMultiplier: $('input[id="eggieSimulationRandomMultiplier"]').val(),
            }),
            'dataType': 'json'

        }, function (data) {
            eggieSimulation = data;
            updateEggieSimulation();
        });
    });
    $("#reset").click(function () {
        events = [];
        refreshDisplay();
    });
    $("#refreshWindowSize").click(function () {
        windowSize = $("#windowSize").val();
    });
});
