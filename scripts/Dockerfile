FROM maven:3.9.0-amazoncorretto-17

ARG access_key
ARG secret_access_key
ARG default_region
ARG profile_name
ENV AWS_ACCESS_KEY=${access_key}
ENV AWS_ACCESS_KEY_ID=${access_key}
ENV AWS_SECRET_ACCESS_KEY=${secret_access_key}
ENV AWS_DEFAULT_REGION=${default_region}
ENV AWS_REGION=${default_region}
ENV PROFILE_NAME=${profile_name}