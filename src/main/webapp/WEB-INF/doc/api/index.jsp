<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
   <title>Swagger UI</title>
   <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,700|Source+Code+Pro:300,600|Titillium+Web:400,600,700" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="../static/swagger/swagger-ui.css" >
    <link rel="icon" type="image/png" href="../static/swagger/favicon-32x32.png" sizes="32x32" />
    <link rel="icon" type="image/png" href="../static/swagger/favicon-16x16.png" sizes="16x16" />
    <style>
      html
      {
        box-sizing: border-box;
        overflow: -moz-scrollbars-vertical;
        overflow-y: scroll;
      }

      *,
      *:before,
      *:after
      {
        box-sizing: inherit;
      }

      body
      {
        margin:0;
        background: #fafafa;
      }
    </style>
</head>
<body>

    <div id="swagger-ui"></div>

    <script src="../static/swagger/swagger-ui-bundle.js"> </script>
    <script src="../static/swagger/swagger-ui-standalone-preset.js"> </script>
    <script>

  window.onload = function() {
      // Begin Swagger UI call region
      const ui = SwaggerUIBundle({
    	url: "../v2/api-docs",
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      })
      // End Swagger UI call region

      window.ui = ui
    }
  </script>
  </body>
</html>