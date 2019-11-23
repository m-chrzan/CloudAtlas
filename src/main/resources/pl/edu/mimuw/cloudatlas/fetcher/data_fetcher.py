import json
import sys
import time
import psutil
import platform
import socket
import configparser
import statistics
import urllib.request

# Pipe
pipe = None

# Attribute params
initial_delay = 0
collection_interval = 1
averaging_period = 1
averaging_method = ""

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


def get_avg_load():
    load_percentages = psutil.cpu_percent(interval=averaging_period, percpu=True)

    if averaging_method == "arithmetic":
        return statistics.mean(load_percentages)
    elif averaging_method == "geometric":
        return statistics.geometric_mean(load_percentages)
    elif averaging_method == "harmonic":
        return statistics.harmonic_mean(load_percentages)
    elif averaging_method == "median":
        return statistics.median(load_percentages)
    else:
        raise RuntimeError("Avg CPU load error")


def get_data():
    avg_load = get_avg_load()
    free_disk = psutil.disk_usage("/").free
    total_disk = psutil.disk_usage("/").total
    free_ram = psutil.virtual_memory().available
    total_ram = psutil.virtual_memory().total
    free_swap = psutil.swap_memory().free
    total_swap = psutil.swap_memory().total
    num_processes = len(psutil.pids())
    num_cores = psutil.cpu_count(False)
    kernel_ver = platform.release()
    logged_users = len(psutil.users())

    external_ip = urllib.request.urlopen('https://ident.me').read().decode('utf8')
    hostname = socket.gethostbyaddr(external_ip)
    dns_names = ([hostname[0]] + hostname[1])[:3]

    sys.stdout.write("[{},{},{},{},{},{},{},{},{},{},{},{}]\n".format(
        avg_load,
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
        json.dumps(dns_names))
    )

    sys.stdout.flush()


def check_averaging_method(averaging_method):
    averaging_methods = ["arithmetic", "geometric", "harmonic", "mean"]
    return averaging_method and averaging_method in averaging_methods


def read_config():
    global initial_delay
    global collection_interval
    global averaging_period
    global averaging_method
    config = configparser.ConfigParser()

    try:
        # check if running from source code dir
        config.read("config.ini")
    except KeyError:
        pass
    else:
        # we assume that it's running as subprocess from Fetcher.java
        # we assume working dir to be CloudAtlas root
        # because gradle seems to put it this way
        config.read("src/main/resources/pl/edu/mimuw/cloudatlas/fetcher/config.ini")

    initial_delay = int(config["AttributeParams"]["initialDelay"])
    collection_interval = int(config["AttributeParams"]["collectionInterval"])
    averaging_period = int(config["AttributeParams"]["averagingPeriod"])
    averaging_method = config["AttributeParams"]["averagingMethod"]

    if not check_averaging_method(averaging_method):
        raise ValueError("Incorrect averaging method")
    elif collection_interval < averaging_period:
        raise ValueError("Collection interval smaller than averaging period")


if __name__ == '__main__':
    read_config()

    time.sleep(initial_delay)
    while True:
        get_data()
        time.sleep(collection_interval - averaging_period)
