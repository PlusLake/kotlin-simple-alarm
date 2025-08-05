all:
	kotlinc -include-runtime -d alarm.jar Main.kt
	jar uvf alarm.jar alarm.wav
	jar uvf alarm.jar font.ttf
run:
	java -jar alarm.jar
