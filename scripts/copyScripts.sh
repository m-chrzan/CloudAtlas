for num in 02 03 04 05; do
	scp scripts/run*.sh "rainbow${num}:~/CloudAtlas/scripts/"
done
