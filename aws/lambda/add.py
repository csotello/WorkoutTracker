#add a new workout
import os
import boto3
import json
import random as rand
from join import join

def handler(event, context):
    """Add a program"""

    resource = boto3.resource("dynamodb")
    program_table = resource.Table("Programs")
    workout_table = resource.Table("Workouts")
    try:
        entry = event
        program = {
            'id':rand.randint(0,10000),
            'Name':entry['Name'],
            'weeks':int(entry['Weeks'])
        }
        program_table.put_item(Item=program)
        return {
            "statusCode":500,
            "body":sched
        }
        sched = json.loads(entry['sched'])
        for day in sched:
            sched[day]['id'] = rand.randint(0,1000000)
            sched[day]['Finished'] = False
            sched[day]['ProgramID'] = program['id']
            workout_table.put_item(Item=sched[day])
        programs = join()
        return {
            'statusCode':200,
            'headers':{},
            'body':json.dumps(programs)
        }
    except:
        return {
            'statusCode':500,
            'headers':{

            },
            'body':"There was an error with our database. Were sorry :("

        }