#!/bin/sh

COMMAND="java -jar build/jars/Sokoban.jar"
success=0
failed=0

for i in `seq $1 $2`; do
    echo -n "$i: "
    elapsed=`(time -p $COMMAND $i -q) 2>&1`
    if [ $? -ne "0" ]; then
        echo -n "failure"
        failed=`expr $failed + 1`
    else
        echo -n "success"
        success=`expr $success + 1`
    fi
    elapsed=`echo "$elapsed" | grep real | sed 's/.* //g'`
    echo ", $elapsed s"
done

echo "Solved: $success"
echo "Not solved: $failed"
