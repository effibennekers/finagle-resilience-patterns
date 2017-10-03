var stompClient = null;


var events = [];
var windowSize = 10;
var effiTps = 0;
var eggieTps = 0;

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
$("#windowSize").text(10);


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
    $.post({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        'type': 'POST',
        'url': '/loadbalancing',
        'data': JSON.stringify({'numberOfRuns': $("#callsPerThread").val(), 'numberOfThreads':$("#numberOfThreads").val()}),
        'dataType': 'json'
    });

}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#loadbalancing").click(function () {
        startLoadbalancing();
    });
    $("#reset").click(function () {
        events = [];
        refreshDisplay();
    });
    $("#refreshWindowSize").click(function () {
        windowSize = $("#windowSize").val();
    });
});
