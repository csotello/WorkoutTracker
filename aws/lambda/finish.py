#Mark a workout as finished
import os, sys
import boto3
import join

def handler(event, context):
    try:
        resource = boto3.resource("dynamodb")
        workout_table = resource.Table("Workouts")
        items = workout_table.scan()
        for item in items["Items"]:
            if str(item['name']) == str(event['name'].strip('"')) and int(item['Day']) == int(event['day']) and int(item['Week']) == int(event['week']):
                workout_table.update_item(
                    Key={
                        "id":item["id"]
                    },
                    UpdateExpression="set Finished=:b",
                    ExpressionAttributeValues={ ':b':True })
                return {
                    "response":join()
                }
        return {
            "response":"No workout Found:" + str(items)
        }
    except Exception as e:
        return {
            "response":str(e)
        }