var events = [];
var windowSize = 10;


var instances = [];
var instanceTable = {};


$(document).ready(function () {
    $('input[id="windowSize"]').val(windowSize);
    $("#stopTest").prop("disabled", true);
    $("#targetTps").val(10);

    $.get("/instances", function (data) {
        instances = data;
        instanceTable = {};
        instances.forEach(function (instance, index) {
            instanceTable[instance.name] = instance;
            addInstanceToHtml(instance, index);
        });
    }, "json");
    refreshDisplay();
});

function updateSimulation(index, data) {
    $('input[id="SimulationBasetime_' + index + '"]').val(data.baseTime);
    $('input[id="SimulationRandom_' + index + '"]').val(data.random);
    $('input[id="SimulationRandomMultiplier_' + index + '"]').val(data.randomMultiplier);
    $('input[id="FailureRate_' + index + '"]').val(data.failureRate);
}

function addInstanceToHtml(instance, index) {
    $("#instance-table").append(
        "<tr><th id='dialogOpener_" + index + "'>" + instance.name + "</th><td id='tps_" + index + "'></td>\
        <td id='Ratio_" + index + "'></td>\
        <td id='successCount_" + index + "'></td>\
        <td id='successAverage_" + index + "'></td>\
        <td id='failureCount_" + index + "'></td>\
        <td id='failureAverage_" + index + "'></td>\
        </tr>"
    );
    $("#instances-container").append(
        "<div id='dialog_" + index + "' class='instanceSettings' title='Settings for " + instance.name + "'>\
           <h1>delay simulation</h1> \
            <div class='table'> \
              <div class='left'> \
                <div>\
                 <label for='SimulationBasetime_" + index + "'>base time</label> \
                </div> \
                <div>\
                 <input type='number' id='SimulationBasetime_" + index + "'> \
                </div> \
              </div> \
              <div class='left'> \
                <div> \
                 <label for='SimulationRandom_" + index + "'>random</label> \
                </div>\
                <div> \
                  <input type='number' id='SimulationRandom_" + index + "'> \
                </div>\
              </div> \
              <div class='left'> \
                <div> \
                  <label for='SimulationRandomMultiplier_" + index + "'>random multiplier</label> \
                </div> \
                <div> \
                  <input type='number' id='SimulationRandomMultiplier_" + index + "'> \
                </div> \
              </div> \
              <div class='left'> \
                <div> \
                  <label for='FailureRate_" + index + "'>failure rate</label> \
                </div> \
                <div> \
                  <input type='number' id='FailureRate_" + index + "'> \
                </div> \
              </div> \
            </div> \
              <div class='left'> \
                <div> \
            <button id='dialogCloser_" + index + "' class='btn btn-default'>close</button> \
                </div>\
                <div> \
            <button id='Settings_" + index + "' class='btn btn-default'>update</button> \
                </div> \
        </div>\
    </div>");

    $("#dialog_" + index).dialog({
        autoOpen: false,
        open: function () {
            $.get("http://" + instance.host + ":" + instance.port + "/simulation", function (data) {
                updateSimulation(index, data);
            }, "json");

        }
    });
    $("#dialogOpener_" + index).click(function () {
        $("#dialog_" + index).dialog("open");
    });
    $("#dialogCloser_" + index).click(function () {
        $("#dialog_" + index).dialog("close");
    });
    $("#Settings_" + index).click(function () {
        $.post({
            crossOrigin: true,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            'type': 'POST',
            'url': 'http://' + instance.host + ':' + instance.port + '/simulation',
            'data': JSON.stringify({
                baseTime: $('input[id="SimulationBasetime_' + index + '"]').val(),
                random: $('input[id="SimulationRandom_' + index + '"]').val(),
                randomMultiplier: $('input[id="SimulationRandomMultiplier_' + index + '"]').val(),
                failureRate: $('input[id="FailureRate_' + index + '"]').val()
            }),
            'dataType': 'json'
        }, function (data) {
            updateSimulation(index, data);
        });
        return false;
    });
}

var eventId = 0;

function registerEvent(event) {
    events.push(event);
    var instance = instanceTable[event.instance];
    if (instance) {
        instance.tps++;
    }
}

$(function () {
    setInterval(refreshDisplay, 1000);
});

var testIntervalId;

function startTest(path) {
    $("#stopTest").prop("disabled", false);
    $('.start-test').each(function () {
        $(this).prop("disabled", true)

    });
    var tps = $("#targetTps").val();
    testIntervalId = setInterval(function () {
        var start;
        $.ajax({
            beforeSend: function () {
                start = new Date().getTime();
            },
            url: path,
            success: function (data) {
                var weather = JSON.parse(data);
                if (!weather.windForce) {
                    weather.windForce = "";
                }
                if (!weather.windDirection) {
                    weather.windDirection = "";
                }
                $("#temperature").text(weather.temperature);
                $("#condition").text(weather.condition);
                $("#windforce").text(weather.windForce);
                $("#winddirection").text(weather.windDirection);

            },
            complete: function (x) {
                var end = new Date().getTime();
                var duration = end - start;
                var event = {
                    instance: x.getResponseHeader("instance"),
                    httpStatus: x.status,
                    duration: duration,
                    id: eventId++
                };
                registerEvent(event);

            }
        });
    }, 1000 / tps);
}

function stopTest() {
    clearInterval(testIntervalId);
    $('.start-test').each(function () {
        $(this).prop("disabled", false)
    });
    $("#stopTest").prop("disabled", true);
}


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

    var totalCounter = 0;
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
            totalCounter++;
        }
    });

    $("#unknownErrors").text(totalCounter);

    var successCounter = 0;
    instances.forEach(function (instance, index) {
        var counter = counterTable[instance.name];
        successCounter += counter.successCounter;
        $("#successCount_" + index).text(counter.successCounter);
        if (counter.successCounter > 0) {
            $("#successAverage_" + index).text(Math.round(counter.successDuration / counter.successCounter));
        }
        else {
            $("#successAverage_" + index).text('-');
        }
        $("#failureCount_" + index).text(counter.failureCounter);
        if (counter.failureCounter > 0) {
            $("#failureAverage_" + index).text(Math.round(counter.failureDuration / counter.failureCounter));
        }
        else {
            $("#failureAverage_" + index).text('-');
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


$(function () {

    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#apachestartloadbalancing").click(function () {
        startTest("/api/apache/loadbalancing");
    });
    $("#stopTest").click(function () {
        stopTest();
    });
    $("#finaglestartloadbalancing").click(function () {
        startTest("/api/finagle/loadbalancing");
    });
    $("#finaglestartfailover").click(function () {
        startTest("/api/finagle/failover");
    });
    $("#apachestartfailover").click(function () {
        startTest("/api/apache/failover");
    });
    $("#apachestartretry").click(function () {
        startTest("/api/apache/retry");
    });
    $("#finaglestartretry").click(function () {
        startTest("/api/finagle/retry");
    });
    $("#reset").click(function () {
        events = [];
        refreshDisplay();
    });
    $("#refreshWindowSize").click(function () {
        windowSize = $("#windowSize").val();
    });
});
