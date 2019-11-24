# Report
[Link](https://www.overleaf.com/5484465583dvszfqpdpztz) to the report.

# Run a Tournament

Put the jar files of your agents in a folder, e.g. ./agents

Create a tournament:
```
java -jar './logist/logist.jar' -new 'tour' './agents'
```
Run the tournament:
```
java -jar './logist/logist.jar' -run 'tour' './config/auction.xml'
```
Save the results:
```
java -jar './logist/logist.jar' -score 'tour'
```