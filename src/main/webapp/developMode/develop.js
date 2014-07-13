var channel = "/developMode" + location.pathname;

(function($){
    var cometdURL = "http://localhost:9990/service/cometd";
    $.cometd.websocketEnabled = false;
    $.cometd.configure({
        url: cometdURL,
        logLevel: 'error'
    });
    
    $.cometd.handshake();

    $.cometd.subscribe(channel ,function(message){
	var expr = message.data.message;
	expr = expr.replace(/\n/g,"");
	if( window.execScript){
	    window.execScript( expr );
	}else{
	    window.eval(expr);
	}
    });
    
})(jQuery);

function giveMe(info){
    $.cometd.publish(channel, {
        message:JSON.stringify(info)
    });
}
