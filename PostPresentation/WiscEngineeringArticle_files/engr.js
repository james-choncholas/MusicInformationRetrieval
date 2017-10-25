
$(function(){
	$('.fancygallery').each(function(){
		var gallery = this;
		var imgCollection = new Array();
		var img;
		$(gallery).find('a').each(function(){
			$(this).attr('data-fancybox-group', $(gallery).attr('id'));
			imgCollection.push(this);
		});	
		$(gallery).children(':not(.ww)').remove();
		$(imgCollection).each(function(){
			$(gallery).append('<div class="fancyimage">' + $(this).wrap('<div>').parent().html() + '<p><a href="' + $(this).attr('href') + '" data-fancybox-group="' + $(gallery).attr('id') + '" class="caption">' + $(this).find('img').attr('alt') + '</a></p></div>');
		});
	});

	$('.fancygallery > .fancyimage > a, .fancygallery .caption').fancybox({
	    beforeShow : function() {
        	var alt = this.element.find('img').attr('alt');
        	this.inner.find('img').attr('alt', alt);
        	this.title = alt;
    	}
	});

});


function showNextFeature(){
	if (currentFeature < (featuredImages.length-1)) nextFeature = currentFeature + 1;
	else nextFeature = 0;
	$(featuredImages[currentFeature]).css('z-index',1);
	$(featuredImages[nextFeature]).css('display','block').css('z-index', 0);
	$(featuredImages[currentFeature]).fadeOut(500);
	if ( currentFeature < (featuredImages.length-1)) {
		currentFeature += 1;
		}
	else currentFeature = 0;
}
function showPrevFeature(){
	if ( currentFeature == 0 ) prevFeature = featuredImages.length-1;
	else prevFeature = currentFeature-1;
	$(featuredImages[currentFeature]).css('z-index',1);
	$(featuredImages[prevFeature]).css('display','block').css('z-index', 0);
	$(featuredImages[currentFeature]).fadeOut(500);
	currentFeature = prevFeature;
}

$(function() {

	featuredImages = $('ul#featured > li');
	if ( featuredImages.length > 1) {
		currentFeature = 0;
		$(featuredImages[currentFeature]).css('display','block');
		var rotation = setInterval(showNextFeature, 6000);
		
		$('a.next').click( function() {
			clearInterval(rotation);
			showNextFeature();
			rotation = setInterval(showNextFeature, 6000);
		});
		
		$('a.previous').click( function() {
			clearInterval(rotation);
			showPrevFeature();
			rotation = setInterval(showNextFeature, 6000);
		});
		
		$('a.pause').click( function() {
			clearInterval(rotation);
		});
	}
	else {
		featuredImages.css('display','block');
		$('a.next').css('display','none');
		$('a.previous').css('display','none');
		$('a.pause').css('display','none');
	}
	
	$('div#center img').each( function() {
		if ( $(this).css('float') == "left" ) {
			$(this).css({'margin-top': '5px', 'margin-right':'10px', 'margin-bottom': '5px'});
		}
		if ( $(this).css('float') == "right" ) {
			$(this).css({'margin-top': '5px', 'margin-left':'10px', 'margin-bottom': '5px'});
		}
	});

	
	
});
		