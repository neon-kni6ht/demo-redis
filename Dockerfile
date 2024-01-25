# Use the official Redis image as the base image
FROM redis:latest

# Install socat
RUN apt-get update && apt-get install -y socat

CMD ["redis-server", "--save", "60", "1", "--loglevel", "warning"]

# Expose the Redis port
EXPOSE 8080

# Set up the entry point to forward traffic from port 8080 to Redis port 6379
ENTRYPOINT ["socat", "TCP-LISTEN:8080,fork", "TCP:localhost:6379"]