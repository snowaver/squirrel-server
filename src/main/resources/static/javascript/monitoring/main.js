$(document).ready
(
	function()
	{
		$(".menubar  a.menubar_parent_item").click( function(){ $(this).siblings("ul").slideToggle(200); } );
		
		var  showingBranch  = $(".menubar  a.menubar_branch_item").first();
		
		addTab( showingBranch.text(),showingBranch.attr("frame-url") );
		
		$(".menubar  a.menubar_branch_item").click( function(){ addTab($(this).text(), $(this).attr("frame-url")); } );
		
		function  updateClass( element,oldClass,newClass )
		{
			$(element).removeClass(oldClass);
			
			$(element).addClass(   newClass);
		}
		
		function  removeTab(tab, prev, next )
		{
			if( $(tab).hasClass( "selected" ) && $(prev).length  >= 1 )
			{
				addTab(  $(prev).text(), $(prev).attr( "frame-url" ) );
			}
			else
			if( $(tab).hasClass( "selected" ) && $(next).length  >= 1 )
			{
				addTab(  $(next).text(), $(next).attr( "frame-url" ) );
			}
			
			$(tab).remove();
		}
		
		function  addTab(    name, frameUrl )
		{
			if( $(".content  div.tabhost  nav>div>a[frame-url='"+frameUrl+"']").length >= 1 )
			{
				$.each( $(".content  div.tabhost  nav>div").children(), (index, element) => updateClass(element, $(element).attr("frame-url") == frameUrl ? "unselect" : "selected", $(element).attr("frame-url") == frameUrl ? "selected" : "unselect") );
			}
			else
			{
				$(".content  div.tabhost  nav>div").append( "<a  href='javascript:void(0)'  frame-url='"+ frameUrl+ "'>"+name+"<span><i  class='fa  fa-times-circle'  aria-hidden='true'></span></a>" );
				
				var  tabContainerWidth   = 0;
				
				$.each( $(".content  div.tabhost  nav>div").children(), (index, element) => {tabContainerWidth = tabContainerWidth+$(element).outerWidth();  updateClass(element, $(element).attr("frame-url") == frameUrl ? "unselect" : "selected", $(element).attr("frame-url") == frameUrl ? "selected" : "unselect");} );
				
				$(".content  div.tabhost  nav>div").css( "width", $(".content  div.tabhost  nav").outerWidth() < tabContainerWidth ? tabContainerWidth : $(".content  div.tabhost  nav").outerWidth() );
				
				$(".content  div.tabhost  nav>div>a[frame-url='"+frameUrl+"']").click( () => addTab(name , frameUrl) );

				$(".content  div.tabhost  nav>div>a[frame-url='"+frameUrl+"']>span>i.fa-times-circle").click( function(){var  tab = $(this).parent().parent();  removeTab( tab , tab.prev() , tab.next() );} );
			}
			
			$(".content  div.tabbody  iframe").attr( "src", frameUrl+"?SECRET_KEY="+$("#secret_key").val() );
		}
						
		$(".content  div.tabhost  i.fa-backward").parent().click( () => $(".content  div.tabhost  nav").scrollLeft($(".content  div.tabhost  nav").scrollLeft()-200) );
		
		$(".content  div.tabhost  i.fa-forward" ).parent().click( () => $(".content  div.tabhost  nav").scrollLeft($(".content  div.tabhost  nav").scrollLeft()+200) );
	}
);