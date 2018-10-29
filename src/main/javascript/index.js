/* eslint-disable  func-names */
/* eslint quote-props: ["error", "consistent"]*/

'use strict';

const Alexa = require('alexa-sdk');
const Http = require('http');
const GAME = 'GAME';
const GAME_ID = 'GAME_ID';
const SESSION_MODE = 'SESSION_MODE';
const SESSION_MODE_GAME = 'GAME';
const SESSION_MODE_HELP = 'HELP';
const SESSION_MODE_RESTART = 'RESTART';
const SESSION_MODE_LINK_ACCOUNT = 'LINK_ACCOUNT';
const APP_ID = 'amzn1.ask.skill.182e5cac-13df-49d6-82b7-085a15448dbc';
const HOST = 'ec2-34-253-223-5.eu-west-1.compute.amazonaws.com';
const PORT = 8080;
const DEBUG = true;
const GAME_PATH_PREFIX = '/quests/';
const GAME_PATH_SUFFIX = '/game.json';
const TEST_GAME_PATH = GAME_PATH_PREFIX + '1' + GAME_PATH_SUFFIX;
const QUEST_LIST_PATH = '/files.json';

const handlers = {
    'LaunchRequest': function () {
        debug('LaunchRequest');
        /*
        if (this.event.session.user.accessToken == undefined) {
            linkAccount(this);
        }
        else { */
        chooseGame(this);
        //}
    },
    'ListStoriesIntent': function () {
        debug('ListStoriesIntent');
        questListRequest(this);
    },
    'PlayIntent': function () {
        debug('PlayIntent');
        questListRequest(this);
    },
    'ChoiceIntent': function () {
        debug('ChoiceIntent');
        questGameRequest(this, true, true);
    },
    'AMAZON.YesIntent': function () {
        debug('YesIntent');
        processYesNo(this, arguments[0]);
    },
    'AMAZON.NoIntent': function () {
        debug('NoIntent');
        processYesNo(this, arguments[0]);
    },
    'AMAZON.HelpIntent': function () {
        debug('HelpIntent');
        this.attributes[SESSION_MODE] = SESSION_MODE_HELP;
        var helpMsg = 'You must choose one of the options by saying "Option" and then the number of your choice. ';
        helpMsg += 'For example you might say "Option 1". ';
        helpMsg += 'Do you want to hear the last section again ?';
        this.emit(':ask', helpMsg, helpMsg);
    },
    'AMAZON.CancelIntent': function () {
        debug('CancelIntent');
        endGame(this);
    },
    'AMAZON.StopIntent': function () {
        debug('StopIntent');
        endGame(this);
    },
    'SessionEndedRequest': function () {
        debug('SessionEndedRequest');
        endGame(this);
    },
};

const chooseGame = function(alexa)
{
    var welcomeMsg = 'Welcome to Quest Player. ';
    welcomeMsg += 'To play an interactive story say "Play" and then the story title. ';
    welcomeMsg += 'To list the stories say "List Stories". ';
    alexa.emit(':ask', welcomeMsg, welcomeMsg);
};

const newGame = function (alexa, includeIntro, linkAccount) {
   clearGame(alexa);
   questGameRequest(alexa, false, true, includeIntro, linkAccount);
};

const clearGame = function (alexa) {
   alexa.attributes[GAME] = null;
};

const endGame = function (alexa) {
   clearGame(alexa);
   alexa.emit(':tell', 'Goodbye');
};

const questListRequest = function (alexa) {

    var params = '', title;
    if(alexa.event.request.intent && alexa.event.request.intent.slots && alexa.event.request.intent.slots.Title)
    {
        title = alexa.event.request.intent.slots.Title.value;
        params += '?title=' + escape(title);
    }

   var responseFn = questListResponseFunction(alexa, title);
   questServiceRequest(QUEST_LIST_PATH+params, 'GET', responseFn);
};

const questListResponseFunction = function (alexa, title)
{
    var me = alexa;
    var searchTitle = title;

    return function(response) {
        response.setEncoding('utf8');
        // data is streamed in chunks from the server
        // so we have to handle the "data" event
        var buffer = '',
            quests,
            text = '',
            eventType = ':ask';

        response.on('data', function (chunk) {
            buffer += chunk;
        });

        response.on('end', function (err) {
            // finished transferring data
            // dump the raw data

            debug(buffer);
            quests = JSON.parse(buffer);

            if(quests.length === 1 && searchTitle)
            {
                // start quest
                alexa.attributes[GAME_ID] = quests[0].id;
                newGame(alexa, true, false);
            }
            else
            {
                if(quests.length === 0)
                {
                    text += 'No quests found with title ' + searchTitle;
                }
                else
                {
                    text += 'Found ' + quests.length + ' stories. Say Play and then the title. Here are the titles...';

                    // list quests
                    quests.forEach(function(q) {
                        text += ensureFullStop(q.title);
                    });
                }

                me.emit(eventType, text, text);
            }
        });
    };
};

const questGameRequest = function (alexa, makeChoice, includeDesc, includeIntro, linkAccount) {
   var game = alexa.attributes[GAME];
   var httpMethod = makeChoice ? 'PUT' : 'GET';
   var requestBody, gameId;

   if(game)
   {
       gameId = game.id;

       if(makeChoice) {
          debug('existing game, id:'+gameId);
          debug('choiceIndex : ' + alexa.event.request.intent.slots.Choice.value);
          game.choiceIndex = alexa.event.request.intent.slots.Choice.value;
          game.choiceId = null;
       }
       requestBody = JSON.stringify(game);
   }
   else
   {
       gameId = alexa.attributes[GAME_ID];
       debug('new game, id:'+gameId);
   }

   var responseFn = questGameResponseFunction(alexa, includeDesc, includeIntro, linkAccount);

   questServiceRequest(GAME_PATH_PREFIX + gameId + GAME_PATH_SUFFIX, httpMethod, responseFn, requestBody);

};

const questGameResponseFunction = function (alexa, includeDesc, includeIntro, linkAccount)
{
    var me = alexa;

    return function(response) {
        response.setEncoding('utf8');
        // data is streamed in chunks from the server
        // so we have to handle the "data" event
        var buffer = '',
            data,
            text = '',
            eventType = ':ask';

        response.on('data', function (chunk) {
            buffer += chunk;
        });

        response.on('end', function (err) {
            // finished transferring data
            // dump the raw data

            debug(buffer);
            data = JSON.parse(buffer);

            me.attributes[GAME] = data.game;
            me.attributes[SESSION_MODE] = SESSION_MODE_GAME;

            if(linkAccount) {
                eventType = ':askWithLinkAccountCard';
                text += 'A card has been sent to the companion app for linking your Amazon account.';
            }

            if(includeIntro)
            {
                text += ensureFullStop(data.game.gameQuest.title + ' by ' + data.game.gameQuest.author);
                text += ensureFullStop(data.game.gameQuest.intro);
            }

            text += getStationText(me, data, includeDesc);

            me.emit(eventType, text, text);
        });
    };
};

const processYesNo = function(alexa, intentName)
{
    var sessionMode = alexa.attributes[SESSION_MODE];
    debug('processYesNo, session mode: "' + sessionMode + '", intent:"' + intentName + '"');

    if (SESSION_MODE_RESTART == sessionMode) {
        'AMAZON.YesIntent' == intentName ? newGame(alexa, false) : endGame(alexa);
    }
    else if (SESSION_MODE_HELP == sessionMode) {
        questGameRequest(alexa, false, 'AMAZON.YesIntent' == intentName, false);
    }
    else if (SESSION_MODE_LINK_ACCOUNT == sessionMode) {
        'AMAZON.YesIntent' == intentName ? newGame(alexa, true, true) : newGame(alexa, true);
    }
    else
    {
        questGameRequest(alexa, false, false, false);
    }
};

const getStationText = function(alexa, data, includeDesc)
{
    var text = '';

    if(includeDesc)
    {
        text += data.gameStation.text + ' ';
    }

    if(data.gameStation.choices.length === 0)
    {
        // set mode to restart
        alexa.attributes[SESSION_MODE] = SESSION_MODE_RESTART;
        text += 'Do you want to play again ?';
    }
    else
    {
        text += 'Here are your choices. ';

        for(var i=0; i<data.gameStation.choices.length; i++)
        {
            text += ' Option ' + (i+1) + ' ' + data.gameStation.choices[i].text;
        }
        text += ' Make your choice.';
    }

    debug('title : ' + data.game.gameQuest.title);
    debug('station : ' + data.gameStation.id);
    debug('number of choices : ' + data.gameStation.choices.length);
    debug('session mode : ' + alexa.attributes[SESSION_MODE]);

    return text;
};

const linkAccount = function(alexa) {

    // set mode to link account
    alexa.attributes[SESSION_MODE] = SESSION_MODE_LINK_ACCOUNT;

    var text = 'Do you want to link your account ?';

    alexa.emit(':ask', text, text);

};

const questServiceRequest = function (path, httpMethod, responseFn, requestBody) {

   debug(httpMethod+ ":" + path + ': ' + requestBody);

   var options = {
       host: HOST,
       path: path,
       port: PORT,
       method: httpMethod,
       headers: {
           'Content-Type': 'application/json',
           'Content-Length': requestBody ? Buffer.byteLength(requestBody) : 0
       }
   };

   var req = Http.request(options, responseFn);
   if(requestBody)
   {
       req.write(requestBody);
   }
   req.end();
};

const debug = function(msg)
{
    if(DEBUG) {
        console.log(msg);
    }
};

const ensureFullStop = function(text)
{
    if(text && text.trim().length > 0 && text.trim().endsWith('.') === false)
    {
        text += '. ';
    }
    return text;
};

exports.handlers = handlers;
exports.APP_ID = APP_ID;

exports.handler = (event, context) => {
    const alexa = Alexa.handler(event, context);
    alexa.APP_ID = APP_ID;
    alexa.registerHandlers(handlers);
    alexa.execute();
};