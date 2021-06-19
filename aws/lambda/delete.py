#Delete an entry from the db
import json
import boto3
import join

def lambda_handler(event, context):
    resource = boto3.resource("dynamodb")
    program_table = resource.Table("Programs")
    workout_table = resource.Table("Workouts")

    program_entries = program_table.scan()
    workout_entries = workout_table.scan()

    for item in program_entries["Items"]:
        if item['Name'] == event['name'].strip('"'):
            for day in workout_entries["Items"]:
                if day["ProgramID"] == item['id']:
                    workout_table.delete_item(Key={'id':day['id']})
            program_table.delete_item(Key={
                'id':item['id'],
                'Name':item['Name']

        })
            return { 'response' : "Program deleted" }
    return { 'response' : "Error" }