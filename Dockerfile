# Use the official Redis image as the base image
FROM redis:latest

# Expose the Redis port
EXPOSE 8080

CMD ["redis-server", "--port", "8080", "--save", "60", "1", "--loglevel", "warning"]
