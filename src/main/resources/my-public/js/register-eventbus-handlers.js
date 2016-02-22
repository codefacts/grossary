if (eb.state == EventBus.CLOSED) {
    alert("Please reload the page. You are now disconnected from the server.");
}

document.addEventListener('EVENT_BUS_DISCONNECTED', function () {
    alert("Please reload the page. You are now disconnected from the server.");
});

if (eb.state === 1) {
    registerEventBusHandlers();
} else {
    document.addEventListener('EVENT_BUS_CONNECTED', function () {

        registerEventBusHandlers();

    });
}


function registerEventBusHandlers() {
    console.log("EB registering ALREADY_LOCKED");
}

(function () {
    console.log("PROTOTYPE_EVENTBUS")
    var send = eb.send.bind(eb);

    eb.send = function (address, message, headers, callback) {
        if (this.state != EventBus.OPEN) {
            console.error("EVENT_BUS DISCONNECTED WHEN REQUESTING -> " + address)
            alert("You have been disconnected from the server. Please reload to reconnect.");
            return;
        }
        send(address, message, headers, callback);
    }
})()