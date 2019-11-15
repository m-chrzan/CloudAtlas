import json
import subprocess
import sys
import time

import schedule
import psutil
import platform
import socket
import configparser
# import sqlite3

# Pipe
pipe = None

# Attribute params
collectionInterval = 1
averagingPeriod = 1
averagingMethod = ""

"""
    Attributes

    the average CPU load (over all cores) as cpu_load
    the free disk space (in bytes) as free_disk
    the total disk space (in bytes) as total_disk
    the free RAM (in bytes) as free_ram
    the total RAM (in bytes) as total_ram
    the free swap (in bytes) as free_swap
    the total swap (in bytes) as total_swap
    the number of active processes as num_processes
    the number of CPU cores as num_cores
    the kernel version as kernel_ver
    the number of users logged in as logged_users
    a set of up to three DNS names of the machine dns_names

"""


def setup_database():
    conn = sqlite3.connect('attributes.db')
    c = conn.cursor()
    c.execute('''CREATE TABLE attrib
             (timestamp integer,
              avg_cpu_load integer,
              free_disk integer,
              total_disk integer,
              free_ram integer,
              total_ram integer,
              free_swap integer,
              total_swap integer,
              num_processes integer,
              num_cores integer,
              kernel_ver text,
              logged_users integer,
              dns_names text) 
              ''')
    # TODO format dns_names
    conn.commit()
    conn.close()


def get_data():
    avg_cpu_load = psutil.cpu_percent(interval=1) # TODO better?
    free_disk = psutil.disk_usage("/").free
    total_disk = psutil.disk_usage("/").total
    free_ram = psutil.virtual_memory().available
    total_ram = psutil.virtual_memory().total
    free_swap = psutil.swap_memory().free
    total_swap = psutil.swap_memory().total
    num_processes = len(psutil.pids())
    num_cores = psutil.cpu_count(False)
    kernel_ver = platform.release() # TODO ew. version()
    logged_users = len(psutil.users())

    hostname = socket.gethostbyaddr("127.0.0.1") # TODO czy dziala
    dns_names = ([hostname[0]] + hostname[1])[:3]
    # https://stackoverflow.com/questions/2575760/python-lookup-hostname-from-ip-with-1-second-timeout

    sys.stdout.write("[{},{},{},{},{},{},{},{},{},{},{},{}]\n".format(
    #print("[{},{},{},{},{},{},{},{},{},{},{},{}]\r\n".format(
    avg_cpu_load,
    free_disk,
    total_disk,
    free_ram,
    total_ram,
    free_swap,
    total_swap,
    num_processes,
    num_cores,
    kernel_ver,
    logged_users,
    json.dumps(dns_names))) #.encode())  # TODO ten string
    sys.stdout.flush()
    # TODO error control and pipe restart

"""
    conn = sqlite3.connect('attributes.db')
    c = conn.cursor()
    c.execute("INSERT INTO attrib VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )",
              avg_cpu_load,
              free_disk,
              total_disk,
              free_ram,
              total_ram,
              free_swap,
              total_swap,
              num_processes,
              num_cores,
              kernel_ver,
              logged_users,
              dns_names.__str__()) # TODO ten string
    conn.commit()
    conn.close()
"""

# TODO funkcja do usuwania zbendych
def remove_historical_data():
    pass


if __name__ == '__main__':
    # config = configparser.ConfigParser()
    # config.read("config.ini")
    collectionInterval = int(5) # int(config["AttributeParams"]["collectionInterval"])
    averagingPeriod = int(5) # int(config["AttributeParams"]["averagingPeriod"])
    averagingMethod = "arithmetic" # config["AttributeParams"]["averagingMethod"]

    # setup_database()

    # TODO some condition for this?
    while True:
        get_data()
        time.sleep(collectionInterval)

    # schedule.every(collectionInterval).seconds.do(get_data)
