'use strict';

require.config({
    baseUrl: '/static/js/',
    shim: {
        'bootstrap' : ['jquery']
    },
    paths: {
        'jquery': 'jquery-2.1.1.min',
        'bootstrap': 'bootstrap.min',
        'knockout': 'knockout-3.2.0'
    }
});

require(['jquery', 'knockout','bootstrap'], function($, ko) {

    var jQuery = $;

    var Value = function(lang, content) {
        this.lang = lang;
        this.content = content;
    };

    var TextTitle = function(lang, content) {
        Value.call(this, lang, content);
    };
    TextTitle.prototype = new Value();

    var TextComment = function(lang, content) {
        Value.call(this, lang, content);
    };
    TextComment.prototype = new Value();

    var Item = function(lang, content, type) {
        this.type = type;
        Value.call(this, lang, content);
    };
    Item.prototype = new Value();

    var Translation = function(lang, content) {
        Value.call(this, lang, content);
    };
    Translation.prototype = new Value();

    var DictItem = function(url) {
        this.type = 'lingvodoc_metaword';
        this.url = url;
    };

    var Word = function(items) {
        this.items = items;
    };
    Word.fromJS = function(word) {
        var items = [];
        for (var i = 0; i < word.items.length; i++) {
            var item = word.items[i];
            if (item.type === 'lingvodoc_metaword') {
                items.push(new DictItem(item.url));
            } else {
                items.push(new Item(item.lang, item.content, item.type));
            }
        }
        return new Word(items);
    };

    var Phrase = function(words, translations) {
        this.words = words;
        this.translations = translations;
    };
    Phrase.fromJS = function(phrase) {
        var i = 0, words = [], translations = [];
        for (i = 0; i < phrase.words.length; i++) {
            var word = phrase.words[i];
            words.push(Word.fromJS(word));
        }

        for (i = 0; i < phrase.translations.length; i++) {
            var translation = phrase.translations[i];
            translations.push(new Translation(translation.lang, translation.content));
        }
        return new Phrase(words, translations);
    };

    var Paragraph = function(phrases) {
        this.phrases = phrases;
    };
    Paragraph.fromJS = function(paragraph) {
        var phrases = [];
        for (j = 0; j < paragraph.phrases.length; j++) {
            var phrase = paragraph.phrases[j];
            phrases.push(Phrase.fromJS(phrase));
        }
        return new Paragraph(phrases);
    };

    var Text = function(text_id, client_id, text_titles, paragraphs) {
        this.text_id = text_id;
        this.client_id = client_id;
        this.text_titles = text_titles;
        this.paragraphs = paragraphs;
    };
    Text.fromJS = function(text) {
        var i = 0, j = 0, k = 0, l = 0;
        var text_titles = [], paragraphs = [];
        for (i = 0; i < text.text_titles.length; i++) {
            text_titles.push(new TextTitle(text.lang, text.content));
        }

        for (i = 0; i < text.paragraphs.length; i++) {
            var paragraph = text.paragraphs[i];
            paragraphs.push(Paragraph.fromJS(paragraph));
        }
        return new Text(text.text_id, text.client_id, text_titles, paragraphs);
    };

    var viewModel = function() {
        var baseUrl = $('#getCorpusUrl').data('lingvodoc');
        this.texts = ko.observableArray([]);

        ko.computed(function() {
            $.getJSON(baseUrl).done(function(response) {
                if (response.corpus_id && response.corpus_client_id) {
                    for (var i = 0; i < response.texts.length; i++) {
                        this.texts().push(Text.fromJS(response.texts[i]));
                    }
                }
            }.bind(this)).fail(function(respones) {
                // TODO: Handle error
            });
        }, this);
    };

    ko.applyBindings(new viewModel());
});