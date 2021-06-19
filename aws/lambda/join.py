#join tables
import os
import boto3
import json

def join():
    resource = boto3.resource("dynamodb")
    program_table = resource.Table("Programs")
    workout_table = resource.Table("Workouts")

    try:
        program_entries = program_table.scan()
        workout_entries = workout_table.scan()
    except:
        return "Database Error"
    join = []
    for program in program_entries['Items']:
        current = {
            'id':int(program['id']),
            'Name':program['Name'],
            'weeks':int(program['weeks']),
            'sched':{}
        }
        for workout in workout_entries['Items']:
            if workout['ProgramID'] == int(program['id']):
                current_workout = {}
                item_name = 'week' + str(workout['Week']) + 'day' + str(workout['Day'])
                current_workout['name'] = workout['name']
                current_workout['ProgramID'] = int(workout['ProgramID'])
                current_workout['Week'] = int(workout['Week'])
                current_workout['Day'] = int(workout['Day'])
                current_workout['Finished'] = workout['Finished'] if 'Finished' in workout else False
                current['sched'][item_name] = current_workout
        join.append(current)
    return join