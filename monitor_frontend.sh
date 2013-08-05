inotifywait -r -m -q -e close_write,moved_to,create ../frontend/src/main/webapp | ./work_frontend.sh
