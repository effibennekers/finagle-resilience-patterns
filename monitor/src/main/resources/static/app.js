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
    connect();
    refreshDisplay();
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

$(function () {
    setInterval(refreshDisplay, 1000);
});

function refreshDisplay() {
    var eventsToProcess = events.slice(-windowSize);

    var effiCounter = [0,0];
    var eggieCounter = [0,0];
    var effiTotal = [0,0];
    var eggieTotal = [0,0];

    $("#effiTps").text(effiTps);
    $("#eggieTps").text(eggieTps);
    effiTps = 0;
    eggieTps = 0;
    eventsToProcess.forEach(function (e) {
        var index = e.httpStatus == 200?0:1;
        console.log('index' + index);
        switch (e.instance) {
            case 'effi':
                effiCounter[index]++;
                effiTotal[index] += e.duration;
                break;

            case 'eggie':
                eggieCounter[index]++;
                eggieTotal[index] += e.duration;
                break;
            default:
                console.log("unable to process event " + JSON.stringify(e));
        }
    });

    for (i = 0; i < 2; i++) {
        if (effiCounter[i] > 0) {
            $("#effi" + i).text('count: ' + effiCounter[i] + ', average: ' + Math.round(effiTotal[i] / effiCounter[i]));
        }
        else {
            $("#effi" + i).text('count: ' + effiCounter[i] + ', average: -');
        }
        var effiCounterTotal = effiCounter[0] + effiCounter[1];
        if (effiCounterTotal > 0) {
            $("#effiRatio").text(Math.round((effiCounter[0]*100)/effiCounterTotal));
        }
        else {
            $("#effiRatio").text("-");
        }

        if (eggieCounter[i] > 0) {
            $("#eggie" + i).text('count: ' + eggieCounter[i] + ', average: ' + +Math.round(eggieTotal[i] / eggieCounter[i]));
        }
        else {
            $("#eggie" + i).text('count: ' + eggieCounter[i] + ', average: -');
        }
        var eggieCounterTotal = eggieCounter[0] + eggieCounter[1];
        if (eggieCounterTotal > 0) {
            $("#eggieRatio").text(Math.round((eggieCounter[0]*100)/eggieCounterTotal));
        }
        else {
            $("#eggieRatio").text("-");
        }

        var overallCounterTotal = effiCounterTotal + eggieCounterTotal;
        if (overallCounterTotal > 0) {
            $("#overallRatio").text(Math.round(((effiCounter[0] +eggieCounter[0])*100)/overallCounterTotal));

        }
        else {
            $("#overallRatio").text("-");
        }
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
