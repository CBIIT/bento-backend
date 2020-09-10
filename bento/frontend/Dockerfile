FROM nginx:alpine

RUN apk upgrade --update \
 && apk --no-cache add tzdata --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community 

COPY dist /usr/share/nginx/html

