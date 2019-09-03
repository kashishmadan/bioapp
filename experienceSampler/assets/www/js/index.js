/*ExperienceSampler License

The MIT License (MIT)

Copyright (c) 2014-2015 Sabrina Thai & Elizabeth Page-Gould

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

//console.log = function() {
//    if (logger.useConsole()) return;
//    logger.log.apply(logger, [].slice.call(arguments));
//};
/* activate localStorage */
var localStore = window.localStorage;
/* surveyQuestion Model (This time, written in "JSON" format to interface more cleanly with Mustache) */
var surveyQuestions = [
						/*0*/
                        {
                            "type": "mult1",
                            "variableName": "snooze",
                            "questionPrompt": "Are you able to take the survey now?",
                            "minResponse": 0,
                            "maxResponse": 1,
                            "labels": [
                                {"label": "No"},
                                {"label": "Yes"}
                            ],
                        },
                        /*1*/
                        {
                            "type": "instructions",
                            "variableName": "generalInstructions",
                            "questionPrompt": "Dans la section ci-dessous, veuillez indiquer comment vous vous sentez en ce moment.Veuillez choisir le chiffre le plus approprié sur l'échelle de six points, où 1 = pas du tout à 6 = tout à fait.",
                        },
                        /*2*/
                        {
                            "type": "mult1",
                            "variableName": "happy",
                            "questionPrompt": "Heureux",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*3*/
                        {
                            "type": "mult1",
                            "variableName": "atEase",
                            "questionPrompt": "A l'aise",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*4*/
                        {
                            "type": "mult1",
                            "variableName": "anxious",
                            "questionPrompt": "Anxieux",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*5*/
                        {
                            "type": "mult1",
                            "variableName": "annoyed",
                            "questionPrompt": "Contrarié",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*6*/
                        {
                            "type": "mult1",
                            "variableName": "motivated",
                            "questionPrompt": "Motivé",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*7*/
                        {
                            "type": "mult1",
                            "variableName": "calm",
                            "questionPrompt": "Calme",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*8*/
                        {
                            "type": "mult1",
                            "variableName": "tired",
                            "questionPrompt": "Fatigué",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*9*/
                        {
                            "type": "mult1",
                            "variableName": "bored",
                            "questionPrompt": "Ennuyé",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*10*/
                        {
                            "type": "mult1",
                            "variableName": "gloomy",
                            "questionPrompt": "Sombre",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*11*/
                        {
                            "type": "mult1",
                            "variableName": "active",
                            "questionPrompt": "Actif",
                            "minResponse": 1,
                            "maxResponse": 6,
                            "labels": [
                                {"label": "1 Pas du tout"},
                                {"label": "2"},
                                {"label": "3"},
                                {"label": "4"},
                                {"label": "5"},
                                {"label": "6 Tout à fait"}
                            ]
                        },
                        /*12*/
                        {
                            "type": "slider",
                            "variableName": "temperature",
                            "questionPrompt": "À l'heure actuelle, à quel point il fait chaud ou froid là où vous êtes, selon vous ?",
//                            "minResponse": 0,
//                            "maxResponse": 40,
                        },
                        /*13*/
                        {
                            "type": "instructions",
                            "variableName": "generalInstructions2",
                            "questionPrompt": "Pour les questions suivantes, nous vous poserons des questions sur vos expériences depuis la dernière fois que vous avez répondu à ce questionnaire. Veuillez répondre aux questions suivantes au sujet de votre partenaire romantique actuel.",
                        },
                        /*14*/
                        {
                            "type": "mult1",
                            "variableName": "partnerListened",
                            "questionPrompt": "Depuis la dernière fois, mon partenaire m'a vraiment écouté",
                            "minResponse": 1,
                            "maxResponse": 9,
                            "labels": [
                                {"label": "1 pas du tout vrai"},
                                {"label": "2"},
                                {"label": "3 pas trop vrai"},
                                {"label": "4"},
                                {"label": "5 moyennement vrai"},
                                {"label": "6"},
                                {"label": "7 vrai"},
                                {"label": "8"},
                                {"label": "9 tout à fait vrai"},
                            ]
                        },
                        /*15*/
                        {
                            "type": "mult1",
                            "variableName": "partnerJudged",
                            "questionPrompt": "Depuis la dernière fois, mon partenaire a très bien jugé mon caractère",
                            "minResponse": 1,
                            "maxResponse": 9,
                            "labels": [
                                {"label": "1 pas du tout vrai"},
                                {"label": "2"},
                                {"label": "3 pas trop vrai"},
                                {"label": "4"},
                                {"label": "5 moyennement vrai"},
                                {"label": "6"},
                                {"label": "7 vrai"},
                                {"label": "8"},
                                {"label": "9 tout à fait vrai"},
                            ]
                        },
                        /*16*/
                        {
                            "type": "mult1",
                            "variableName": "partnerResponsive",
                            "questionPrompt": "Depuis la dernière fois, mon partenaire était réactif à mes besoins",
                            "minResponse": 1,
                            "maxResponse": 9,
                            "labels": [
                                {"label": "1 pas du tout vrai"},
                                {"label": "2"},
                                {"label": "3 pas trop vrai"},
                                {"label": "4"},
                                {"label": "5 moyennement vrai"},
                                {"label": "6"},
                                {"label": "7 vrai"},
                                {"label": "8"},
                                {"label": "9 tout à fait vrai"},
                            ]
                        },
                        /*17*/
                        {
                            "type": "instructions",
                            "variableName": "generalInstructions3",
                            "questionPrompt": "Veuillez répondre aux questions suivantes à votre sujet.",
                        },
                        /*18*/
                        {
                            "type": "mult1",
                            "variableName": "meListened",
                            "questionPrompt": "Depuis la dernière fois, j'ai vraiment écouté mon partenaire",
                            "minResponse": 1,
                            "maxResponse": 9,
                            "labels": [
                                {"label": "1 pas du tout vrai"},
                                {"label": "2"},
                                {"label": "3 pas trop vrai"},
                                {"label": "4"},
                                {"label": "5 moyennement vrai"},
                                {"label": "6"},
                                {"label": "7 vrai"},
                                {"label": "8"},
                                {"label": "9 tout à fait vrai"},
                            ]
                        },
                        /*19*/
                        {
                            "type": "mult1",
                            "variableName": "meJudged",
                            "questionPrompt": "Depuis la dernière fois, j'étais un excellent juge du caractère de mon partenaire.",
                            "minResponse": 1,
                            "maxResponse": 9,
                            "labels": [
                                {"label": "1 pas du tout vrai"},
                                {"label": "2"},
                                {"label": "3 pas trop vrai"},
                                {"label": "4"},
                                {"label": "5 moyennement vrai"},
                                {"label": "6"},
                                {"label": "7 vrai"},
                                {"label": "8"},
                                {"label": "9 tout à fait vrai"},
                            ]
                        },
                        /*20*/
                        {
                            "type": "mult1",
                            "variableName": "meResponsive",
                            "questionPrompt": "Depuis la dernière fois, j'étais réactif aux besoins de mon partenaire.",
                            "minResponse": 1,
                            "maxResponse": 9,
                            "labels": [
                                {"label": "1 pas du tout vrai"},
                                {"label": "2"},
                                {"label": "3 pas trop vrai"},
                                {"label": "4"},
                                {"label": "5 moyennement vrai"},
                                {"label": "6"},
                                {"label": "7 vrai"},
                                {"label": "8"},
                                {"label": "9 tout à fait vrai"},
                            ]
                        },
                        /*21*/
                        {
                            "type": "text",
                            "variableName": "day",
                            "questionPrompt": "Parlez-nous très brièvement de votre journée en général et de vos interactions avec votre partenaire depuis la dernière fois que nous vous avons demandé.",
                        },
						/*0*/
//                       {
//                       "type": "mult1",
//                       "variableName": "snooze",
//                       "questionPrompt": "Are you able to take the survey now?",
//                       "minResponse": 0,
//                       "maxResponse": 1,
//                       "labels": [
//                                {"label": "No"},
//                                {"label": "Yes"}
//                                ],
//                       },
//                    	/*1*/
//                       {
//                       "type": "instructions",
//                       "variableName": "generalInstructions",
//                       "questionPrompt": "On the following screens, we will be asking you questions about your experiences since we last beeped you.",
//                       },
//                       /*2*/
//                       {
//                       "type": "mult2",
//                       "variableName": "mood",
//                       "questionPrompt": "Please indicate how you feel right now.",
//                       "minResponse": -3,
//                       "maxResponse": 3,
//                       "labels":[
////                                {"label": "3 Test"},
//                                {"label": "3 Very positive"},
//                                {"label": "2 Somewhat positive"},
//                                {"label": "1 A little positive"},
//                                {"label": "0 Neutral"},
//                                {"label": "-1 A little negative"},
//                                {"label": "-2 Somewhat negative"},
//                                {"label": "-3 Very negative"},
//                                ]
//                       },
//                       /*3*/
//                       {
//                       "type": "text",
//                       "variableName": "mostStressfulEvent",
//                       "questionPrompt": "What the most stressful thing you experienced today?",
//                       },
//                       /*4*/
//                       {
//                       "type": "slider",
//                       "variableName": "stressThermometer",
//                       "questionPrompt": "Please indicate the amount of stress you are experiencing at the moment by moving the slider up or down. 0 means you are experiencing no stress and 100 means you are experiencing the highest amount of stress. You can see the value to the right of the slider.",
//                       "minResponse": 0,
//                       "maxResponse": 100,
//                       },
//                       /*5*/
//                       {
//                       "type": "mult1",
//                       "variableName": "experienceConflict",
//                       "questionPrompt": "Did you experience any conflicts since the last time you completed the survey?",
//                       "minResponse": 0,
//                       "maxResponse": 1,
//                       "labels": [
//                                {"label": "No"},
//                                {"label": "Yes"}
//                                ],
//                       },
//                       /*6*/
//                       {
//                       "type": "text",
//                       "variableName": "conflictParticipant",
//                       "questionPrompt": "Who was the primary person involved in this conflict?",
//                       },
//                       /*7*/
//                       {
//                       "type": "mult1",
//                       "variableName": "otherConflictParticipant",
//                       "questionPrompt": "Was anyone else involved?",
//                       "minResponse": 0,
//                       "maxResponse": 1,
//                       "labels": [
//                                {"label": "No"},
//                                {"label": "Yes"}
//                                ],
//                       },
//                       /*8*/
//                       {
//                       "type": "text",
//                       "variableName": "otherConflictParticipantNames",
//                       "questionPrompt": "Who else was involved in this conflict? Please list the names of the other people involved in the conflict.",
//                       },
//                       /*9*/
//                       {
//                       "type": "mult1",
//                       "variableName": "closeness",
//                       "questionPrompt": "How close do you feel to NAME right now?",
//                       "minResponse": 0,
//                       "maxResponse": 6,
//                       "labels": [
//                                {"label": "0 Not at all close"},
//                                {"label": "1"},
//                                {"label": "2"},
//                                {"label": "3"},
//                                {"label": "4"},
//                                {"label": "5"},
//                                {"label": "6 Extremely close"}
//                                ],
//                       },
//                       /*10*/
//                       {
//                       "type": "mult1",
//                       "variableName": "conflictTopic",
//                       "questionPrompt": "What was the conflict about? Please select from the list of options below.",
//                       "minResponse": 1,
//                       "maxResponse": 11,
//                       "labels":[
//                                {"label": "Money"},
//                                {"label": "Sex"},
//                                {"label": "Work"},
//                                {"label": "Children"},
//                            	{"label": "Chores"},
//                            	{"label": "Communication"},
//                            	{"label": "Jealousy"},
//                            	{"label": "Lack of Consideration"},
//                            	{"label": "Lack of Respect"},
//                            	{"label": "Differences in Opinions"},
//                            	{"label": "Other"},
//                                ],
//                       },
//                       /*11*/
//                       {
//                       "type": "text",
//                       "variableName": "conflictTopicOther",
//                       "questionPrompt": "Please specify what you mean by 'other.'",
//                       },
//                       /*12*/
//                       {
//                       "type": "mult1",
//                       "variableName": "conflictResolution",
//                       "questionPrompt": "Was the conflict resolved?",
//                       "minResponse": 0,
//                       "maxResponse": 1,
//                       "labels": [
//                                {"label": "No"},
//                                {"label": "Yes"},
//                                ],
//                       },
//                       /*13*/
//                       {
//                       "type": "mult1",
//                       "variableName": "conflictSupport",
//                       "questionPrompt": "Did you seek support or talk to anyone after the conflict?",
//                       "minResponse": 0,
//                       "maxResponse": 1,
//                       "labels": [
//                                {"label": "No"},
//                                {"label": "Yes"},
//                                ],
//                       },
//                       /*14*/
//                       {
//                       "type": "text",
//                       "variableName": "conflictSupporter",
//                       "questionPrompt": "Who did you seek support from following this conflict? Please also indicate your relationship to this person. (e.g., Mike S. - Coach)",
//                       },
//                       /*15*/
//                       {
//                       "type": "checklist",
//                       "variableName": "typeOfSupport",
//                       "questionPrompt": "Please tell us what kind of support this person provided. Please select as many as applicable.",
//                       "minResponse": 1,
//                       "maxResponse": 4,
//                       "labels": [
//                                {"label": "Emotional support (e.g., listened to you, cheered you up)"},
//                                {"label": "Esteem support (e.g., encouraged you, boosted your confidence)"},
//                                {"label": "Informational support (e.g., gave you advice, offered ideas and suggestions)"},
//                                {"label": "Tangible support (e.g., helped you with tasks)"},
//                                ],
//                       },
//                       /*16*/
//                       {
//                       "type": "mult1",
//                       "variableName": "otherConflictSupporters",
//                       "questionPrompt": "Did you seek support from anyone else following this conflict?",
//                       "minResponse": 0,
//                       "maxResponse": 1,
//                       "labels": [
//                                {"label": "No"},
//                                {"label": "Yes"},
//                                ],
//                       }
];
var lastPage = [
                {
                "message": "Thank you for completing today’s questions. Please wait while the data is sent to our servers..."
                },
                {
                "message": "That's cool! I'll notify you again in 10 minutes!"
                },
                {
                "message": "Thank you for installing our app. Please wait while the data is sent to our servers..."
                }
                ];
var participantSetup = [
                        {
                        "type": "text",
                        "variableName": "participant_id",
                        "questionPrompt": "Please enter your participant ID:"
                        },
//                        {
//                    	"type": "timePicker",
//                        "variableName": "weekdayWakeTime",
//                        "questionPrompt": "Please select the time that you usually wake up on WEEKDAYS:"
//                        },
//                        {
//                    	"type": "timePicker",
//                        "variableName": "weekdayDinnerTime",
//                        "questionPrompt": "Please select the time that you usually have dinner on WEEKDAYS:"
//                        },
//                        {
//                        "type": "timePicker",
//                        "variableName": "weekendWakeTime",
//                        "questionPrompt": "Please select the time that you usually wake up on WEEKENDS:"
//                        },
//                        {
//                        "type": "timePicker",
//                        "variableName": "weekendDinnerTime",
//                        "questionPrompt": "Please select the time that you usually have dinner on WEEKENDS:"
//                        },
                        ];

/*Populate the view with data from surveyQuestion model*/
// Making mustache templates
//Here you declare global variables are well
var NUMSETUPQS = participantSetup.length;
var SNOOZEQ = 0;
var questionTmpl = "<p>{{{questionText}}}</p><ul>{{{buttons}}}</ul>";
var questionTextTmpl = "{{{questionPrompt}}}";
var buttonTmpl = "<li><button id='{{id}}' value='{{value}}'>{{label}}</button></li>";
var textTmpl = "<li><textarea cols=50 rows=5 id='{{id}}'></textarea></li><li><button type='submit' value='Enter'>Enter</button></li>";
var checkListTmpl =  "<li><input type='checkbox' id='{{id}}' value='{{value}}'>{{label}}</input></li>";
var instructionTmpl = "<li><button id='{{id}}' value = 'Next'>Next</button></li>";
var sliderTmpl = "<li><input type='range' min='{{min}}' max='{{max}}' value='{{value}}' orient=vertical id='{{id}}' oninput='outputUpdate(value)'></input><output for='{{id}}' id='slider'>50</output><script>function outputUpdate(slidervalue){document.querySelector('#slider').value=slidervalue;}</script></li><li><button type='submit' value='Enter'>Enter</button></li>";
var datePickerTmpl = '<li><input id="{{id}}" data-format="DD-MM-YYYY" data-template="D MMM YYYY" name="date"><br /><br /></li><li><button type="submit" value="Enter">Enter</button></li><script>$(function(){$("input").combodate({firstItem: "name",minYear:2015, maxYear:2016});});</script>';
var dateAndTimePickerTmpl = '<li><input id="{{id}}" data-format="DD-MM-YYYY-HH-mm" data-template="D MMM YYYY  HH:mm" name="datetime24"><br /><br /></li><li><button type="submit" value="Enter">Enter</button></li><script>$(function(){$("input").combodate({firstItem: "name",minYear:2015, maxYear:2016});});</script>';
var timePickerTmpl = "<li><input id ='{{id}}' type='time'></input><br /><br /></li><li><button type='submit' value='Enter'>Enter</button></li>";
var lastPageTmpl = "<h3>{{message}}</h3>";
var uniqueKey;
var name;

var app = {
    // Application Constructor
initialize: function() {
    this.bindEvents();
},
    // Bind Event Listeners
bindEvents: function() {
    document.addEventListener("deviceready", this.onDeviceReady, false);
    document.addEventListener("resume", this.onResume, false);
    document.addEventListener("pause", this.onPause, false);
},
onDeviceReady: function() {
    app.init();
},
onResume: function() {app.sampleParticipant();},
onPause: function() {app.pauseEvents();},
//Beginning our app functions
/* The first function is used to specify how the app should display the various questions. You should note which questions
should be displayed using which formats before customizing this function*/
renderQuestion: function(question_index) {
    //First load the correct question from the JSON database
    var question;
    if (question_index <= -1) {question = participantSetup[question_index + NUMSETUPQS];}
    else {question = surveyQuestions[question_index];}
    var questionPrompt = question.questionPrompt;
	if (questionPrompt.indexOf('NAME') >= 0) {
		questionPrompt = questionPrompt.replace("NAME", function replacer() {return name;});
      	}
    question.questionText = Mustache.render(questionTextTmpl, {questionPrompt: questionPrompt});

    //Now populate the view for this question, depending on what the question type is
    switch (question.type) {
    	case 'mult1': // Rating scales (i.e., small numbers at the top of the screen and larger numbers at the bottom of the screen).
    		question.buttons = "";
        	var label_count = 0;
        	for (var i = question.minResponse; i <= question.maxResponse; i++) {
            	var label = question.labels[label_count++].label;
            	question.buttons += Mustache.render(buttonTmpl, {
                                                id: question.variableName+i,
                                                value: i,
                                                label: label
                                                });
        	}
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	$("#question ul li button").click(function(){
        		app.recordResponse(this, question_index, question.type);
        	});
        	break;
        case 'mult2': // Rating scales (i.e., positive numbers at the top of the screen and negative numbers at the bottom of the screen).
    		question.buttons = "";
            var label_count = 0;
            for (var j = question.maxResponse; j >= question.minResponse; j--) {
                var label = question.labels[label_count++].label;
                question.buttons += Mustache.render(buttonTmpl, {
                                                    id: question.variableName+j,
                                                    value: j,
                                                    label: label
                                                    });
            }
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	$("#question ul li button").click(function(){
        		app.recordResponse(this, question_index, question.type);
        	});
        	break;
        case 'checklist':
        	question.buttons = "";
        	var label_count = 0;
        	var checkboxArray = [];
        	for (var i = question.minResponse; i <= question.maxResponse; i++) {
            	var label = question.labels[label_count++].label;
            	question.buttons += Mustache.render(checkListTmpl, {
                                                	id: question.variableName+i,
                                                	value: i,
                                                	label: label
                                                	});
        	}
        	question.buttons += "<li><button type='submit' value='Enter'>Enter</button></li>";
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	$("#question ul li button").click( function(){
                                          checkboxArray.push(question.variableName);
                                          $.each($("input[type=checkbox]:checked"), function(){checkboxArray.push($(this).val());});
                                          app.recordResponse(String(checkboxArray), question_index, question.type);
            });
            break;
        case 'slider':
        	question.buttons = Mustache.render(sliderTmpl, {id: question.variableName+"1"}, {min: question.minResponse}, {max: question.maxResponse}, {value: (question.maxResponse)/2});
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	var slider = [];
        	$("#question ul li button").click(function(){
        			slider.push(question.variableName);
        			slider.push($("input[type=range]").val());
        			app.recordResponse(String(slider), question_index, question.type);
        	});
        	break;
        case 'instructions':
        	question.buttons = Mustache.render(instructionTmpl, {id: question.variableName+"1"});
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	var instruction = [];
        	$("#question ul li button").click(function(){
        		instruction.push(question.variableName);
        		instruction.push($(this).val());
        		app.recordResponse(String(instruction), question_index, question.type);
        	});
        	break;
        case 'text': //default to open-ended text
        	 question.buttons = Mustache.render(textTmpl, {id: question.variableName+"1"});
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	$("#question ul li button").click(function(){
				if (app.validateResponse($("textarea"))){
        		 	app.recordResponse($("textarea"), question_index, question.type);
                }
                else {
                    alert("Please enter something.");
                }
            });
            break;
        case 'datePicker':
        	question.buttons = Mustache.render(datePickerTmpl, {id: question.variableName+"1"});
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	var date, dateSplit, variableName = [], dateArray = [];
        	$("#question ul li button").click(function(){
        		date = $("input").combodate('getValue');
        		dateArray.push(question.variableName);
        		dateArray.push(date);
        		app.recordResponse(String(dateArray), question_index, question.type);
        	});
        	break;
        case 'dateAndTimePicker':
        	question.buttons = Mustache.render(dateAndTimePickerTmpl, {id: question.variableName+"1"});
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	var date, dateSplit, variableName = [], dateArray = [];
        	$("#question ul li button").click(function(){
        		date = $("input").combodate('getValue');
        		dateArray.push(question.variableName);
        		dateArray.push(date);
        		app.recordResponse(String(dateArray), question_index, question.type);
        	});
        	break;
        case 'timePicker':
        	question.buttons = Mustache.render(timePickerTmpl, {id: question.variableName+"1"});
        	$("#question").html(Mustache.render(questionTmpl, question)).fadeIn(400);
        	var time, timeSplit, variableName = [], timeArray = [];
        	$("#question ul li button").click(function(){
				if (app.validateTime($("input"))){
        		 	app.recordResponse($("input"), question_index, question.type);
                }
                else {
                    alert("Please enter a time.");
                }
        	});
        	break;
        }
},

renderLastPage: function(pageData, question_index) {
    $("#question").html(Mustache.render(lastPageTmpl, pageData));
    if ( question_index == SNOOZEQ ) {
        app.snoozeNotif();
        localStore.snoozed = 1;
        app.saveData();
    }
    else if ( question_index == -1) {
    	app.saveDataLastPage();
    }
    else {
    	var datestamp = new Date();
    	var year = datestamp.getFullYear(), month = datestamp.getMonth(), day=datestamp.getDate(), hours=datestamp.getHours(), minutes=datestamp.getMinutes(), seconds=datestamp.getSeconds(), milliseconds=datestamp.getMilliseconds();
    	localStore[uniqueKey + '.' + "completed" + "_" + "completedSurvey"  + "_" + year + "_" + month + "_" + day + "_" + hours + "_" + minutes + "_" + seconds + "_" + milliseconds] = 1;
    	app.saveDataLastPage();
    }
},
    /* Record User Responses */
recordResponse: function(button, count, type) {
    //Record date (create new date object)
    var datestamp = new Date();
    var year = datestamp.getFullYear(), month = datestamp.getMonth(), day=datestamp.getDate(), hours=datestamp.getHours(), minutes=datestamp.getMinutes(), seconds=datestamp.getSeconds(), milliseconds=datestamp.getMilliseconds();
    //Record value of text field
    var response, currentQuestion, uniqueRecord;
    if (type == 'text') {
        response = button.val();
        // remove newlines from user input
        response = response.replace(/(\r\n|\n|\r)/g, ""); //encodeURIComponent(); decodeURIComponent()
        currentQuestion = button.attr('id').slice(0,-1);
    }
    else if (type == 'slider') {
    	response = button.split(/,(.+)/)[1];
        currentQuestion = button.split(",",1);
    }
    //Record the array
    else if (type == 'checklist') {
        response = button.split(/,(.+)/)[1];
        currentQuestion = button.split(",",1);
    }
    else if (type == 'instructions') {
    	response = button.split(/,(.+)/)[1];
        currentQuestion = button.split(",",1);
    }
    //Record value of clicked button
    else if (type == 'mult1') {
        response = button.value;
        //Create a unique identifier for this response
        currentQuestion = button.id.slice(0,-1);
    }
    //Record value of clicked button
    else if (type == 'mult2') {
        response = button.value;
        //Create a unique identifier for this response
        currentQuestion = button.id.slice(0,-1);
    }
    else if (type == 'datePicker') {
		response = button.split(/,(.+)/)[1];
     	currentQuestion = button.split(",",1);
    }
    else if (type == 'dateAndTimePicker') {
		response = button.split(/,(.+)/)[1];
     	currentQuestion = button.split(",",1);
    }
    else if (type == 'timePicker') {
    	response = button.val();
        currentQuestion = button.attr('id').slice(0,-1);
    }
    console.log('record response: ', count);
    if (count == 6) {name = response;}
    if (count <= -1) {uniqueRecord = currentQuestion;}
    else {uniqueRecord = uniqueKey + "_" + currentQuestion + "_" + year + "_" + month + "_" + day + "_" + hours + "_" + minutes + "_" + seconds + "_" + milliseconds;}
//     //Save this to local storage
    localStore[uniqueRecord] = response;
    console.log('record response');
    //Identify the next question to populate the view
    //This is where you do the Question Logic
    if (count <= -1) {console.log(uniqueRecord);}
   	if (count == -1) {app.scheduleNotifs(); app.renderLastPage(lastPage[2], count);}
    else if (count == SNOOZEQ && response == 0) {app.renderLastPage(lastPage[1], count);}
//    else if (count == 5 && response == 0) {app.renderLastPage(lastPage[0], count);}
//    else if (count == 5 && response == 1) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(6);});}
//    else if (count == 7 && response == 0) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(9);});}
//    else if (count == 7 && response == 1) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(8);});}
//    else if (count == 10 && response < 11) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(12);});}
//    else if (count == 10 && response == 11) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(11);});}
//    else if (count == 13 && response == 0) {app.renderLastPage(lastPage[0], count);}
//    else if (count == 13 && response == 1) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(14);});}
//    else if (count == 16 && response == 0) {app.renderLastPage(lastPage[0], count);}
//    else if (count == 16 && response == 1) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(14);});}
    else if (count < surveyQuestions.length-1) {$("#question").fadeOut(400, function () {$("#question").html("");app.renderQuestion(count+1);});}
    else {app.renderLastPage(lastPage[0], count);}
},
    /* Prepare for Resume and Store Data */
    /* Time stamps the current moment to determine how to resume */
pauseEvents: function() {
    localStore.pause_time = new Date().getTime();
    app.saveData();
},
    /* Initialize the whole thing */
init: function() {
    uniqueKey = new Date().getTime();
    if (localStore.participant_id === " " || !localStore.participant_id) {app.renderQuestion(-NUMSETUPQS);}
    else {
    	uniqueKey = new Date().getTime();
        localStore.uniqueKey = uniqueKey;
	var startTime = new Date(uniqueKey);
    	var syear = startTime.getFullYear(), smonth = startTime.getMonth(), sday=startTime.getDate(), shours=startTime.getHours(), sminutes=startTime.getMinutes(), sseconds=startTime.getSeconds(), smilliseconds=startTime.getMilliseconds();
    	localStore[uniqueKey + "_" + "startTime"  + "_" + syear + "_" + smonth + "_" + sday + "_" + shours + "_" + sminutes + "_" + sseconds + "_" + smilliseconds] = 1;
        app.renderQuestion(0);
    }
    localStore.snoozed = 0;
},

sampleParticipant: function() {
    var current_moment = new Date();
    var current_time = current_moment.getTime();
    if ((current_time - localStore.pause_time) > 600000 || localStore.snoozed == 1) {
        uniqueKey = new Date().getTime();
    	var startTime = new Date(uniqueKey);
    	var syear = startTime.getFullYear(), smonth = startTime.getMonth(), sday=startTime.getDate(), shours=startTime.getHours(), sminutes=startTime.getMinutes(), sseconds=startTime.getSeconds(), smilliseconds=startTime.getMilliseconds();
    	localStore[uniqueKey + "_" + "startTime"  + "_" + syear + "_" + smonth + "_" + sday + "_" + shours + "_" + sminutes + "_" + sseconds + "_" + smilliseconds] = 1;
        localStore.snoozed = 0;
        app.renderQuestion(0);
    }
    else {
        uniqueKey = localStore.uniqueKey;
    }
    app.saveData();
},
saveData:function() {
    console.log('saveData:', localStore);
    $.ajax({
           type: 'post',
//           type: 'get',
           url: 'https://cgi.socialthermo.ovh/cgi-bin/data_collector.cgi',
//           url: 'https://script.google.com/macros/s/AKfycbzzbp0437BkTqx95W9THF9JhWcydzn-K-FJTbwIHF23-S0JbDXG/exec',
           data: localStore,
           crossDomain: true,
           success: function (result) {
               var pid = localStore.participant_id, snoozed = localStore.snoozed,
                    uniqueKey = localStore.uniqueKey, pause_time = localStore.pause_time;
               localStore.clear();
               localStore.participant_id = pid;
               localStore.snoozed = snoozed;
               localStore.uniqueKey = uniqueKey;
               localStore.pause_time = pause_time;
           },
           processData: false,
           error: function (request, error) {console.log(error);}
           });
},
saveDataLastPage:function() {
    console.log('saveDataLastPage: ', localStore);
//    var data = {};
//    for (var elem in localStore) {
//        data[elem] = localStore[elem];
//    }
    var data = '';
    var first = true;
    for (var elem in localStore) {
        if(first) {
            first = false;
        } else {
            data += '&';
        }
        data += elem + '=' + localStore[elem];
    }
    console.log('data: ', data);
    $.ajax({
           type: 'post',
//           type: 'get',
//           url: 'https://anthony-dallagnola.tk/cgi-bin/data_collector.cgi',
           url: 'https://cgi.socialthermo.ovh/cgi-bin/data_collector.cgi',
//           url: 'https://www.cgi.socialthermo.ovh/cgi-bin/data_collector.cgi',
//           url: 'https://script.google.com/macros/s/AKfycbzzbp0437BkTqx95W9THF9JhWcydzn-K-FJTbwIHF23-S0JbDXG/exec',
           data: data,
//           data: localStore,
//           json: true,
           crossDomain: true,
           success: function (result) {	
           		var pid = localStore.participant_id, snoozed = localStore.snoozed, uniqueKey = localStore.uniqueKey;
           		localStore.clear();
            	localStore.participant_id = pid;
           		localStore.snoozed = snoozed;
           		localStore.uniqueKey = uniqueKey;
           		$("#question").html("<h3>Your responses have been recorded. Thank you for completing this survey.</h3>");
           },
//           processData: false,
           error: function (request, error) {
           		console.log(error);
                $("#question").html("<h3>Please try resending data. If problems persist, please contact the researchers.</h3><br><button>Resend data</button>");
                $("#question button").click(function () {app.saveDataLastPage();});           		
           	}
           });
},
scheduleNotifs:function() {
//    return;
	//cordova.plugins.backgroundMode.enable();
   	var interval1, interval2, interval3, interval4, interval5, interval6, interval7
   	var a, b, c, d, e, f, g;
   	var date1, date2, date3, date4, date5, date6, date7;
   	var currentMaxHour, currentMaxMinutes, currentMinHour, currenMinMinutes, nextMinHour, nextMinMinutes;
   	var currentLag, dinnerLag, maxInterval;
   	// added
   	var nightlyLag;
   	var day = 86400000; // 1 day
	var today = new Date();
	today.setHours(0,0,0,0);
	var now = new Date();
//    var today = dateObject.getTime();
//    var dayOfWeek = dateObject.getDay(), currentHour = dateObject.getHours(), currentMinute = dateObject.getMinutes();
   	for (i = 0; i < 14; i ++) {

   		interval1 = i * day + (9 * 3600 + parseInt(Math.round(Math.random() * 2 * 3600)))*1000;
        interval2 = i * day + (12 * 3600 + parseInt(Math.round(Math.random() * 4 * 3600)))*1000;
        interval3 = i * day + (19 * 3600 + parseInt(Math.round(Math.random() * 2 * 3600)))*1000;

        date1 = new Date(today.getTime() + interval1);
        date2 = new Date(today.getTime() + interval2);
        date3 = new Date(today.getTime() + interval3);

        a = 101+(parseInt(i)*100);
        b = 102+(parseInt(i)*100);
        c = 103+(parseInt(i)*100);

        if(now < date1) {
            cordova.plugins.notification.local.schedule({icon: 'ic_launcher', id: a, trigger: {at: date1}, text: 'Time for your next Diary Survey!', title: 'Diary Survey'});
        }
        if(now < date2) {
            cordova.plugins.notification.local.schedule({icon: 'ic_launcher', id: b, trigger: {at: date2}, text: 'Time for your next Diary Survey!', title: 'Diary Survey'});
        }
        if(now < date3) {
            cordova.plugins.notification.local.schedule({icon: 'ic_launcher', id: c, trigger: {at: date3}, text: 'Time for your next Diary Survey!', title: 'Diary Survey'});
        }

//        console.log('date2');
        console.log(date1);
        console.log(date2);
        console.log(date3);

        // the timer starts from today at midnight so we need to be sure we won't set a timer before now, a solution would be start only the day after
        localStore['notification_' + i + '_1'] = localStore.participant_id + "_" + a + "_" + date1;
        localStore['notification_' + i + '_2'] = localStore.participant_id + "_" + b + "_" + date2;
        localStore['notification_' + i + '_3'] = localStore.participant_id + "_" + c + "_" + date3;
    }
},
snoozeNotif:function() {
//    var now = new Date().getTime(), snoozeDate = new Date(now + 600*1000);
    var now = new Date().getTime(), snoozeDate = new Date(now + 600*1000);
//    var now2 = new Date();
//    now2.setHours(0,0,0,0);
//    var notifDate = new Date(now2.getTime() + (12 + 2) * 3600 * 1000 + 9 * 60 * 1000 + 30 * 1000);
//    console.log(notifDate);
//    var now = new Date().getTime(), snoozeDate = new Date(now + 3*1000);
    var id = '99';
    console.log(snoozeDate);
    console.log('id: ' + id.toString());
//    console.log(Object.keys(cordova.plugins.notification.local));
//    console.log(cordova.plugins.notification.local);

    cordova.plugins.notification.local.schedule([{
                                         icon: 'ic_launcher',
//                                         channel: 'experience sampler',
                                         id: 2,
                                         title: 'Diary Survey',
                                         text: 'Please complete survey now!',
                                         trigger: {
                                         at: snoozeDate,
                                         },
                                         },
                                         ]);

//                                             cordova.plugins.notification.local.schedule(
//                                             );
  //console.log(snoozeDate);                                       
},     
validateResponse: function(data){
        var text = data.val();
//         console.log(text);
        if (text === ""){
        	return false;
        } else { 
        	return true;
        }
    }, 
validateTime: function(data){
	var time = data.val();
	if (time=== ""){
		return false	
	}
	else {
		return true
	}
}       
};


/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
