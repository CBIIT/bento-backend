# Pull the base image from Alpine
FROM nginx:alpine AS base

##### DEV BUILD STEPS: COPY PREBUILT CODE INTO CONTAINER #####

FROM base AS dev
RUN echo "Building DEV Install.."

COPY frontend/source /usr/share/nginx/html

##############################################################

##### DEMO BUILD STEPS: BUIL CODE FROM GITHUB ################

FROM base AS demo
RUN echo "Building DEMO Install..."
RUN apk add --virtual .build-deps --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community \
 git \
 npm

ARG LOCAL_IP

# clone bento frontend repo
RUN git clone https://github.com/CBIIT/bento-frontend.git /usr/local/bento-frontend \
 && cd /usr/local/bento-frontend \
 && echo "Passing the backend address as env var:  ${LOCAL_IP}" \
 && echo "REACT_APP_BACKEND_API=http://${LOCAL_IP}:8084/v1/graphql/" > .env \
 && npm install \
 && npm install --save https://github.com/skiran86/mui-custom-datatables/tarball/master \
 && cd /usr/local/bento-frontend/node_modules/mui-custom-datatables \
 && npm install \
 && npm run build \
 && cd /usr/local/bento-frontend \
 && npm run build

# Copy website to nginx html dir
RUN cp -r /usr/local/bento-frontend/dist/* /usr/share/nginx/html

# Cleanup build
RUN apk del .build-deps \
 && rm -rf /usr/local/bento-frontend

##############################################################
