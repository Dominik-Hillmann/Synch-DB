# Run script every day on hours 0, 4, 8, ... on minute zero.
0 0,4,8,12,16,20 * * * /home/dominik/Desktop/Synch_On_Intervalls.sh

# View cron tabs:
# crontab -u dominik -l

# To remove specific crons, type
# EDITOR="gedit" crontab -e
# And edit the cron jobs and save the file.

