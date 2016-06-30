echo "Deployment started"
scp ~/Documents/ideaWS/context-sharing-server/target/context-sharing-server-1.0-SNAPSHOT.zip ubuntu@ec2-54-152-1-96.compute-1.amazonaws.com:/home/ubuntu
cat ./run_on_server.sh | ssh -q ubuntu@ec2-54-152-1-96.compute-1.amazonaws.com