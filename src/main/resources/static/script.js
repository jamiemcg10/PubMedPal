$(()=>{ // when document has loaded
    let botui = new BotUI('my-botui-app');  // initialize bot
    let messageBox = document.getElementById('message');
    
    const ele = $('.botui-messages-container')[0];
    const config = {childList: true, subtree: true};
    const callback = function(mutationsList, observer){  // function for when DOM tree changes
        const newMutation = mutationsList[mutationsList.length-1];   
            if (newMutation.type === 'childList'){ // if new child was added
                let lastBotMessage = $('.botui-message-content').last(); // get last message from bot
                    if (!lastBotMessage.hasClass("human")){ // confirm that message was from bot and not human
                        try {
                            let eventBound = $._data( $(lastBotMessage)[0], "events" );
                            if (checkOverflow(lastBotMessage[0]) & eventBound === undefined){  // message is bigger than normal box
                                lastBotMessage.addClass('msg-closed');  // minimize message
                                $(lastBotMessage).click(()=>{  // add event listener to message to toggle between open and closed
                                    if ($(lastBotMessage).hasClass('msg-closed')){
                                        $(lastBotMessage).addClass('msg-open');
                                        $(lastBotMessage).removeClass('msg-closed');
                                    } else if ($(lastBotMessage).hasClass('msg-open')){
                                        $(lastBotMessage).addClass('msg-closed');
                                        $(lastBotMessage).removeClass('msg-open');
                                    }
                                });
                            }
                        } catch (error) {
                            ;
                        }
                    }
                }
    }
            
    const observer = new MutationObserver(callback);  // create Mutation observer to listen for changes to DOM
    observer.observe(ele, config); // start MutationObserver
                        
    startBot();

    $('#send-btn').click(()=>{
        if (messageBox.value != ""){ // do nothing if message is empty
            let method = 0;
            let searchRequest = messageBox.value;
            botui.message.human({  // show message
                content: messageBox.value
            }).then(()=>{   // handle message
                if (searchRequest.toLowerCase().includes("thank you") || searchRequest.toLowerCase().includes("thanks") ){
                    messageBox.value = "";
                    botui.message.bot({
                        content: "You're welcome!"
                    });
                } else if (searchRequest.toLowerCase() === "help" || searchRequest.toLowerCase() === "help me" || searchRequest.toLowerCase().includes("what can you do") || searchRequest.toLowerCase() === "commands") {
                    messageBox.value = "";
                    botui.message.bot({
                        content: "You can say things like \"Search for articles on cancer\", \"Show me articles about obesity after 2018\", \"How many articles about leukemia are there?\", or \"I want to see my search history\""
                    });
                } else {                 
                    sendParseRequestToServer(searchRequest);  // figure out what message means
                }
                
            });
        }
    });

    function startBot(){ 
        botui.message.bot({  // show greeting message
            content: 'Hello there!',
            delay: 1000
        }).then(()=>{ // after message is shown
            botui.message.bot({ // show next message
                content: 'How can I help you?',
                delay: 1500
            });
        });
        $('#message').focus();
    }

    function sendParseRequestToServer(searchRequest){
        botui.message.bot({
            loading: true,
            type: 'html',
        }).then((index)=>{
            // send message to server
            let body = { "requestText": searchRequest};
            messageBox.value = "";
            $.ajax({
                url: '/api/parse', 
                type: 'POST',
                contentType: 'application/json',
                data: body,
                success: function(data, textStatus, jqXHR){ 
                    data = JSON.parse(data);

                    // have request type - handle or send actual request
                    if (data.requestType === "undefined"){ // can't tell what request is
                        botui.message.update(index, {
                            loading: false,
                            content: "Sorry, I don't understand what you're saying."
                        });
                    } else if (data.requestType === "get search history"){
                        let searchHistory = `Frequencies:<ul>${data.searchHistory.frequencies}</ul>&nbsp;<br/>Descriptions:<ol>${data.searchHistory.descriptions}</ol>`;
                        botui.message.update(index, {
                            loading: false,
                            content: searchHistory
                        });
                    } else { // actual search request
                        botui.message.update(index, {
                            loading: false,
                            content: "How do you want to search?"
                        }).then((index2)=>{
                            botui.action.button({
                                delay: 200,
                                addMessage: true,
                                action: [{
                                    text: "Brute force",
                                    value: 1
                                },
                                {
                                    text: "Lucene",
                                    value: 2
                                },
                                {
                                    text: "MongoDB",
                                    value: 3
                                },
                                {
                                    text: "MySQL",
                                    value: 4
                                }],
                                autoHide: true
                            }).then((res)=>{
                                method = res.value;
                                sendSearchRequestToServer(method, data.term, data.originalTerm, data.startDate, data.endDate, data.getCount); // search
                            });                      
                        });
                    }

                    
                },
            });
        });
    }

    function sendSearchRequestToServer(method, term, originalTerm, startDate, endDate, getCount){
        botui.message.bot({
            loading: true,
            type: 'text',
        }).then((index)=>{
            // send message to server
            let body = `{ "method": ${method},
                "term": ${term},
                "originalTerm": ${originalTerm},
                "startDate": "${startDate}",
                "endDate": "${endDate}",
                "getCount": "${getCount}"
                }`;
            messageBox.value = "";
            $.ajax({
                url: '/api/search', 
                type: 'POST',
                contentType: 'application/json',
                data: body,
                success: function(data, textStatus, jqXHR){ 
                    data = JSON.parse(data);
                    let msgContent;
                    if (data.results === ""){ // results are empty
                        msgContent = "Sorry, I couldn't find anything.";
                        botui.message.update(index, {
                            loading: false,
                            content: msgContent
                        });
                    } else if (data.results === "Sorry, something went wrong. Please refresh the page."){
                        msgContent = data.results;
                        botui.message.update(index, {
                            loading: false,
                            content: msgContent
                        });
                    } else {
                        if (data.getCount === "true"){
                            msgContent = `There are ${data.results} articles about ${data.shortSearchDescription}`
                                botui.message.update(index, {
                                loading: false,
                                content: msgContent
                            });
                        } else {
                            msgContent = `Here are results for ${data.shortSearchDescription}`
                                botui.message.update(index, {
                                loading: false,
                                content: msgContent
                            });
                            botui.message.bot({
                                content: `<ol>${data.results}</ol>`,
                                type: 'html',
                            });
                        }
                    }

                    $('#message').focus();
                },
                error: function(jqXHR, textStatus, errorThrown){ 
                    botui.message.update(index, {
                        loading: false,
                        content: "Sorry, something went wrong. Try again later."
                    });
                }
            });
        });
    }

    $('#message').on("keydown", (event)=>{ // send message if user hits Enter key
        if (event.code === "Enter"){
         $('#send-btn').click();   
        }
    });

    $( window ).on("beforeunload", (event)=>{  // tell server to close resources
        observer.disconnect();
        $.ajax({
            url: '/api/close', 
            type: 'GET',
        });
    });

    // Determines if the passed element is overflowing its bounds, either vertically or horizontally.
    // Will temporarily modify the "overflow" style to detect this
    // if necessary.
    function checkOverflow(el){
        let curOverflow = el.style.overflow;
        
        if ( !curOverflow || curOverflow === "visible" ){
            el.style.overflow = "hidden";
        }
        
        let isOverflowing = el.clientWidth < el.scrollWidth || el.clientHeight < el.scrollHeight;
        
        el.style.overflow = curOverflow;
        
        return isOverflowing; 
    }
});