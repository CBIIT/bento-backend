#!/bin/bash
input="statefile"
while IFS= read -r line
do
  terraform state rm "$line"
done < "$input"
