# Use the official Redis image as the base image
FROM redis:latest

CMD ["redis-server", "--port", "8080", "--save", "60", "1", "--loglevel", "warning"]

# Expose the Redis port
EXPOSE 8080