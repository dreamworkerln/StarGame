StackExchange.postValidation=function(){function e(e,t,n){var i={"Title":".js-post-title-field","Body":".js-post-body-field[data-post-type-id="+t+"]","Tags":".js-post-tags-field","Mentions":".js-post-mentions-field","EditComment":".js-post-edit-comment-field","Excerpt":".js-post-excerpt-field","Email":".js-post-email-field"};return i[n]?e.find(i[n]):$()}function t(t,n,i){var o=e(t,n,i);return i===U||i===L?t.find(".js-tag-editor").filter(function(){return $(this).data("target-field")===o.get(0)}):o}function n(e,t,n,i){var o=e.find('input[type="submit"]:visible, button[type="submit"]:visible'),r=o.length&&o.is(":enabled");r&&o.prop("disabled",!0),l(e,t,n),u(e,t,n,i),p(e,t,n),h(e,t,n),g(e,t,n),w(e,t,function(){d(e,t,n),r&&o.prop("disabled",!1)})}function i(i,s,c,l,u){n(i,s,c,l);var d,p=function(e){if(e.success)if(u)u(e);else{var t=window.location.href.split("#")[0],n=e.redirectTo.split("#")[0];0===n.indexOf("/")&&(n=window.location.protocol+"//"+window.location.hostname+n),d=!0,window.location=e.redirectTo,t.toLowerCase()===n.toLowerCase()&&window.location.reload(!0)}else e.captchaHtml?StackExchange.nocaptcha.init(e.captchaHtml,p):e.errors?(i.find(".js-post-prior-attempt-count").val(function(e,t){return(+t+1||0).toString()}),k(i,s,c,e.errors,e.warnings)):b(i,c,{"General":[$("<span/>").text(e.message).html()]},0)};i.submit(function(){if(i.find(".js-post-answer-while-asking-checkbox").is(":checked"))return!0;if(f(i,s,c))return StackExchange.helpers.enableSubmitButton(i),!1;if(a(),StackExchange.navPrevention&&StackExchange.navPrevention.stop(),i.find('input[type="submit"]:visible, button[type="submit"]').addClass("is-loading"),StackExchange.helpers.disableSubmitButton(i),StackExchange.options.site.enableNewTagCreationWarning){var n=e(i,s,U),l=n.prop("defaultValue");if(n.val()!==l)return $.ajax({"type":"GET","url":"/posts/new-tags-warning","dataType":"json","data":{"tags":n.val()},"success":function(e){if(e.showWarning){var n={"closeOthers":!0,"shown":function(){$(".js-confirm-tag-creation").on("click",function(e){return StackExchange.helpers.closePopups(),r(i,c,d,p),e.preventDefault(),!1})},"dismissing":function(){o(i,d)},"returnElements":t(i,s,U).find("input:visible")};StackExchange.helpers.showModal($(e.html).elementNodesOnly(),n),StackExchange.helpers.bindMovablePopups()}else r(i,c,d,p)}}),!1}return setTimeout(function(){r(i,c,d,p)},0),!1})}function o(e,t){e.find('input[type="submit"]:visible, button[type="submit"]').removeClass("is-loading"),t||StackExchange.helpers.enableSubmitButton(e)}function r(e,t,n,i){$.ajax({"type":"POST","dataType":"json","data":e.serialize(),"url":e.attr("action"),"success":i,"error":function(){var n=v(t,0);b(e,t,{"General":[$("<span/>").text(n).html()]},0)},"complete":function(){o(e,n)}})}function a(){for(var e=0;e<F.length;e++)clearTimeout(F[e]);F=[]}function s(t,n,i,o,r){e(t,n,o).blur(function(){var e=this,a=$(this),s=function(e){E(t,n,i,o,e)},c=function(e){return x(e,t,n,i,[o])};F.push(setTimeout(function(){var t=StackExchange.stacksValidation.handlerFor(a);t&&!M&&t.clear(),r.call(e,a,s,c)},V))})}function c(t,n,i,o,r){if(1===n&&"question"===i)return x({"type":"POST","url":"/posts/validate-question","data":{"title":e(t,n,P).val(),"body":e(t,n,D).val(),"tags":e(t,n,U).val(),"fkey":StackExchange.options.user.fkey}},t,n,i,[P,D,U],r).promise();if(2===n)return x({"type":"POST","url":"/posts/validate-body","data":{"body":e(t,n,D).val(),"oldBody":e(t,n,D).prop("defaultValue"),"isQuestion":!1,"isSuggestedEdit":o||!1,"fkey":StackExchange.options.user.fkey}},t,n,i,[D],r).promise();var a=$.Deferred();return a.reject(),a.promise()}function l(e,t,n){s(e,t,n,P,function(e,t,n){var i=e.val(),o=$.trim(i).length,r=e.data("min-length"),a=e.data("max-length");return 0!==o||M?r&&r>o?(t(function(e){return 1==e.minLength?"Title must be at least "+e.minLength+" character.":"Title must be at least "+e.minLength+" characters."}({"minLength":r})),void 0):a&&o>a?(t(function(e){return 1==e.maxLength?"Title cannot be longer than "+e.maxLength+" character.":"Title cannot be longer than "+e.maxLength+" characters."}({"maxLength":a})),void 0):(n({"type":"POST","url":"/posts/validate-title","data":{"title":i,"fkey":StackExchange.options.user.fkey}}),void 0):(t(),void 0)})}function u(e,t,n,i){s(e,t,n,D,function(e,n,o){var r=e.val(),a=$.trim(r).length,s=e.data("min-length");return 0!==a||M?5===t?(s&&s>a?n(function(e){return"Wiki Body must be at least "+e.minLength+" characters. You entered "+e.actual+"."}({"minLength":s,"actual":a})):n(),void 0):((1===t||2===t)&&o({"type":"POST","url":"/posts/validate-body","data":{"body":r,"oldBody":e.prop("defaultValue"),"isQuestion":1===t,"isSuggestedEdit":i,"fkey":StackExchange.options.user.fkey}}),void 0):(n(),void 0)})}function d(e,t,n){s(e,t,n,U,function(e,t,n){var i=e.val(),o=$.trim(i).length;return 0!==o||M?(n({"type":"POST","url":"/posts/validate-tags","data":{"tags":i,"oldTags":e.prop("defaultValue"),"fkey":StackExchange.options.user.fkey},"success":function(t){var n=e.closest(".js-post-form").find(".js-warned-tags-field");if(n.length){var i=n.val(),o=n.data("warned-tags")||[],r=((t.source||{}).Tags||[]).filter(function(e){return e&&-1===o.indexOf(e)});r.length>0&&StackExchange.using("gps",function(){r.forEach(function(e){StackExchange.gps.track("tag_warning.show",{"tag":e},!0),i+=" "+e,o.push(e)}),n.val($.trim(i)).data("warned-tags",o),StackExchange.gps.sendPending()})}}}),void 0):(t(),void 0)})}function f(t,n,i){return"[Edit removed during grace period]"===$.trim(e(t,n,N).val())?(E(t,n,i,N,"Comment reserved for system use. Please use an appropriate comment."),!0):!1}function p(e,t,n){s(e,t,n,N,function(i,o){var r=i.val(),a=$.trim(r).length,s=i.data("min-length"),c=i.data("max-length");return 0===a?(o(),void 0):s&&s>a?(o(function(e){return 1==e.minLength?"Your edit summary must be at least "+e.minLength+" character.":"Your edit summary must be at least "+e.minLength+" characters."}({"minLength":s})),void 0):c&&a>c?(o(function(e){return 1==e.maxLength?"Your edit summary cannot be longer than "+e.maxLength+" character.":"Your edit summary cannot be longer than "+e.maxLength+" characters."}({"maxLength":c})),void 0):(f(e,t,n)||o(),void 0)})}function h(e,t,n){s(e,t,n,q,function(e,t){var n=e.val(),i=$.trim(n).length,o=e.data("min-length"),r=e.data("max-length");return 0===i?(t(),void 0):o&&o>i?(t(function(e){return"Wiki Excerpt must be at least "+e.minLength+" characters; you entered "+e.actual+"."}({"minLength":o,"actual":i})),void 0):r&&i>r?(t(function(e){return"Wiki Excerpt cannot be longer than "+e.maxLength+" characters; you entered "+e.actual+"."}({"maxLength":r,"actual":i})),void 0):(t(),void 0)})}function g(e,t,n){s(e,t,n,R,function(e,t){var n=e.val(),i=$.trim(n),o=i.length;return 0===o?(t(),void 0):StackExchange.helpers.isEmailAddress(i)?(t(),void 0):(t("This email does not appear to be valid."),void 0)})}function m(e,t){var n=$("#sidebar, .sidebar").first().width()||270,i="lg"===StackExchange.responsive.currentRange();return e===B?{"position":"inline","css":{"display":"inline-block","margin-bottom":"10px"},"closeOthers":!1,"dismissable":!1,"type":t}:{"position":{"my":i?"left top":"top center","at":i?"right center":"bottom center"},"css":{"max-width":n,"min-width":n},"closeOthers":!1,"type":t}}function v(e,t){if(t>0)switch(e){case"question":return function(e){return 1==e.specificErrorCount?"Your question couldn't be submitted. Please see the error above.":"Your question couldn't be submitted. Please see the errors above."}({"specificErrorCount":t});case"answer":return function(e){return 1==e.specificErrorCount?"Your answer couldn't be submitted. Please see the error above.":"Your answer couldn't be submitted. Please see the errors above."}({"specificErrorCount":t});case"edit":return function(e){return 1==e.specificErrorCount?"Your edit couldn't be submitted. Please see the error above.":"Your edit couldn't be submitted. Please see the errors above."}({"specificErrorCount":t});case"tags":return function(e){return 1==e.specificErrorCount?"Your tags couldn't be submitted. Please see the error above.":"Your tags couldn't be submitted. Please see the errors above."}({"specificErrorCount":t});default:return function(e){return 1==e.specificErrorCount?"Your post couldn't be submitted. Please see the error above.":"Your post couldn't be submitted. Please see the errors above."}({"specificErrorCount":t})}else switch(e){case"question":return"An error occurred submitting the question.";case"answer":return"An error occurred submitting the answer.";case"edit":return"An error occurred submitting the edit.";case"tags":return"An error occurred submitting the tags.";default:return"An error occurred submitting the post."}}function b(e,t,n,i){var o=e.find(".js-general-error").text("").removeClass("d-none");if(!C(e,o,n,null,B,t))return i>0?(o.text(v(t,i)),void 0):(o.addClass("d-none"),void 0)}function y(e){var t=$(".js-post-review-summary").closest(".js-post-review-summary-container");if(t.length>0)return t.filter(":visible").scrollIntoView(),void 0;var n;A()&&($("#sidebar").animate({"opacity":.4},500),n=setInterval(function(){A()||($("#sidebar").animate({"opacity":1},500),clearInterval(n))},500));var i;e.find(".validation-error, .js-stacks-validation.has-error").each(function(){var e=$(this).offset().top;(!i||i>e)&&(i=e)});var o=function(){for(var t=0;3>t;t++)e.find(".message").animate({"left":"+=5px"},100).animate({"left":"-=5px"},100)};if(i){var r=$(".review-bar").length;i=Math.max(0,i-(r?125:30)),$("html, body").animate({"scrollTop":i},o)}else o()}function k(e,t,n,i,o){i&&w(e,t,function(){var r=S(e,t,n,[P,D,U,L,N,q,R],i,o).length;b(e,n,i,r),y(e)})}function w(e,n,i){var o=function(){1!==n||t(e,n,U).length?i():setTimeout(o,250)};o()}function x(e,t,n,i,o,r){return $.ajax(e).then(function(e){return r?$.when(r()).then(function(){return e}):e}).done(function(e){S(t,n,i,o,e.errors,e.warnings)}).fail(function(){S(t,n,i,o,{},{})})}function S(e,n,i,o,r,a){for(var s=[],c=0;c<o.length;c++){var l=o[c];C(e,t(e,n,l),r,a,l,i)&&s.push(l)}return s}function E(e,n,i,o,r){j(e,t(e,n,o),r?[$("<span/>").text(r).html()]:[],[],o,i)}function C(e,t,n,i,o,r){var a=n[o]||[],s=(i||{})[o]||[];return j(e,t,a,s,o,r)}function j(e,t,n,i,o,r){var a=StackExchange.stacksValidation.handlerFor(t);return a?T(a,r,n,i,o):O(t,o,n),e.find(".validation-error, .js-stacks-validation.has-error").length||e.find(".js-general-error").text(""),t.trigger("post:validated-field",[r,o,n,i]),n.length>0}function T(e,t,n,i){e.clear("error"),n.forEach(function(t){e.add("error",t)}),"edit"===t||"question"===t&&M||(e.clear("warning"),i.forEach(function(t){e.add("warning",t)}))}function O(e,t,n){e&&e.length&&(0===n.length||1===n.length&&""===n[0]||!$("html").has(e).length?_(e):I(e,n,m(t,"error")))}function I(e,t,n){var i=1===t.length?t[0]:"<ul><li>"+t.join("</li><li>")+"</li></ul>",o=e.data("error-popup");if(o&&o.is(":visible")){var r=e.data("error-message");if(r===i)return o.animateOffsetTop&&o.animateOffsetTop(0),void 0;o.fadeOutAndRemove()}var s=StackExchange.helpers.showMessage(e,i,n);s.find("a").attr("target","_blank"),s.click(a),e.addClass("validation-error").data("error-popup",s).data("error-message",i)}function _(e){var t=e.data("error-popup");t&&t.is(":visible")&&t.fadeOutAndRemove(),e.removeClass("validation-error"),e.removeData("error-popup"),e.removeData("error-message")}function A(){var e=!1,t=$("#sidebar, .sidebar").first();if(!t.length)return!1;var n=t.offset().left;return $(".message").each(function(){var t=$(this);return t.offset().left+t.outerWidth()>n?(e=!0,!1):void 0}),e}var M=$("body").hasClass("js-ask-page-v2"),P="Title",D="Body",U="Tags",L="Mentions",N="EditComment",q="Excerpt",R="Email",B="General",F=[],V=250;return{"initOnBlur":n,"initOnBlurAndSubmit":i,"showErrorsAfterSubmission":k,"validatePostFields":c,"scrollToErrors":y}}();