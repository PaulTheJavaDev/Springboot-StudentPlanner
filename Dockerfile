FROM nginx:alpine

# Lösche Standardinhalt von nginx
RUN rm -rf /usr/share/nginx/html/*

# Kopiere alle Dateien aus public/ nach nginx
COPY ./public /usr/share/nginx/html

# Setze login/index.html als Startseite
RUN cp /usr/share/nginx/html/login/index.html /usr/share/nginx/html/index.html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
