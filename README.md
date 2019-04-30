# xianliaoPlugin
A cordova plugin, a JS version of xianliao SDK

<html>
	<body>
		<script type="text/javascript" src="cordova.js"></script>
		<script type="text/javascript" src="jquery-3.3.1.min.js" ></script>
		<script type="text/javascript">
			function isInstalled(){
				jhxianliao.isInstalled(function (installed) {
						alert("xianliao installed: " + (installed ? "Yes" : "No"));
					}, function (reason) {
						alert("Failed: " + reason);
					});
			}
			
			function loginByXl(){
				try{
					var scope = "snsapi_userinfo";
					jhxianliao.auth(scope, function (response) {
						// you may use response.code to get the access token.
						alert(JSON.stringify(response));
					}, function (reason) {
						alert("Failed: " + reason);
					});
				}catch(e){alert(e);}
			}
			//分享卡片
			function shareCard(){
				jhxianliao.share({
					message: {
						title: "Hi, there",
						description: "This is description.",
						thumb: "www/source/thumbnail.png",
						mediaTagName: "TEST-TAG-001",
						messageExt: "这是第三方带的测试字段",
						media: {
							type: jhxianliao.Type.LINK,
							webpageUrl: "http://www.jiahuagame.com"
						}
					},
			//		scene: jhxianliao.Scene.TIMELINE   // share to Timeline
				}, function () {
					alert("Success");
				}, function (reason) {
					alert("Failed: " + reason);
				});
			}
			//分享图片
			function shareImg(){
				jhxianliao.share({
					message: {
						title: "Hi, there",
						description: "This is description.",
						thumb: "www/source/thumbnail.png",
						mediaTagName: "TEST-TAG-001",
						messageExt: "这是第三方带的测试字段",
						media:{
							type: jhxianliao.Type.IMAGE,
							image: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAO4AAABWCAYAAADId6o6AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyFpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTQyIDc5LjE2MDkyNCwgMjAxNy8wNy8xMy0wMTowNjozOSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIChXaW5kb3dzKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDpGOEZDMDM1NEFDMkYxMUU4ODc2NkU5OUE0MjU4QjhFMyIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDpGOEZDMDM1NUFDMkYxMUU4ODc2NkU5OUE0MjU4QjhFMyI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOkY4RkMwMzUyQUMyRjExRTg4NzY2RTk5QTQyNThCOEUzIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOkY4RkMwMzUzQUMyRjExRTg4NzY2RTk5QTQyNThCOEUzIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8++SHqrAAAC4VJREFUeNrsnX9sVeUZ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"
						}
					},
			//		scene: jhxianliao.Scene.TIMELINE   // share to Timeline
				}, function () {
					alert("Success");
				}, function (reason) {
					alert("Failed: " + reason);
				});
			}
			
			function checkLogin(){
				
			}
			
		
	</script>
	<button  onclick="isInstalled()">是否安装</button>
	<button  onclick="loginByXl()">登陆</button>
	<button  onclick="shareCard()">分享卡片</button>
	<button  onclick="shareImg()">分享图片</button>
	</body>
</html>
