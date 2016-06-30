for PID in `pgrep -f context-sharing-server`
do
    echo "Killing context-sharing-server with PID=$PID..."
    kill $PID 2> /dev/null
done

cd /home/ubuntu

echo "Removing folder with context-sharing-server..."
rm -rf context-sharing-server
mkdir context-sharing-server

echo "Unzipping context-sharing-server..."
unzip -q context-sharing-server-1.0-SNAPSHOT.zip -d /home/ubuntu/context-sharing-server

cd context-sharing-server

echo "Launching context-sharing-server..."
nohup java -cp lib -jar context-sharing-server-1.0-SNAPSHOT.jar > output.log &

echo "context-sharing-server is alive check"
timeout 180 wget --retry-connrefused --tries 180 --waitretry=1  --spider http://127.0.0.1:9000/v1/isAlive?database=true 2>&1 | grep --quiet "200 OK"
if [[ $? -eq 0 ]]; then
 echo "Deployment of context-sharing-server finished"
 exit 0
else
 echo "context-sharing-server endpoint doesn't respond"
 exit 1
fi