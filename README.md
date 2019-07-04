# TAIM
Aim of this project is a worklog management app which supports different sources.

*Still under construction.*

Supports:
* InMemory
* Very specific excel file
* SOON -> SQL

# What to consider

There is an _appliction.conf_ in the resources folder.
You will find instructions there.

## IN-MEMROY
The data will be stored in a Map.

## EXCEL
This is a very specific excel file. 
It is planned to make it configurable in which columns the working time is written.

## SQL
At the moment only SQLite (from xerial) is supported.  
The following tables will be created (if not exists):
* WORK_DAY
  * day DATE (PK)

* WORK_PARTS
  * day DATE (FK -> WORK_DAY) (PK)
  * start DATE (PK)
  * end DATE (PK)