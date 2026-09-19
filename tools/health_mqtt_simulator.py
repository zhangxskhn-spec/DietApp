import argparse
import json
import random
import time
from datetime import datetime

import paho.mqtt.client as mqtt


def now_iso() -> str:
    return datetime.now().isoformat(timespec="seconds")


def generate_heart_rate() -> dict:
    bpm = random.randint(68, 118)
    status = "normal"
    if bpm >= 105:
        status = "high"
    elif bpm <= 72:
        status = "rest"
    return {
        "ts": int(time.time() * 1000),
        "time": now_iso(),
        "value": bpm,
        "unit": "bpm",
        "status": status,
    }


def generate_blood_pressure() -> dict:
    systolic = random.randint(108, 138)
    diastolic = random.randint(68, 88)
    status = "normal"
    if systolic >= 135 or diastolic >= 85:
        status = "warning"
    return {
        "ts": int(time.time() * 1000),
        "time": now_iso(),
        "systolic": systolic,
        "diastolic": diastolic,
        "unit": "mmHg",
        "status": status,
    }


def generate_blood_glucose() -> dict:
    glucose = round(random.uniform(4.6, 8.8), 1)
    status = "normal"
    if glucose >= 7.8:
        status = "high"
    elif glucose <= 4.9:
        status = "low"
    return {
        "ts": int(time.time() * 1000),
        "time": now_iso(),
        "value": glucose,
        "unit": "mmol/L",
        "status": status,
    }


def generate_exercise() -> dict:
    steps = random.randint(1200, 9600)
    calories = random.randint(120, 680)
    cadence = random.randint(78, 124)
    return {
        "ts": int(time.time() * 1000),
        "time": now_iso(),
        "steps": steps,
        "calories": calories,
        "cadence": cadence,
        "status": "active" if cadence > 100 else "light",
    }


def publish(client: mqtt.Client, topic: str, payload: dict) -> None:
    text = json.dumps(payload, ensure_ascii=False)
    client.publish(topic, text, qos=0, retain=False)
    print(f"[PUB] {topic} -> {text}")


def main() -> None:
    parser = argparse.ArgumentParser(description="MQTT health data simulator")
    parser.add_argument("--host", default="127.0.0.1", help="MQTT broker host")
    parser.add_argument("--port", type=int, default=1883, help="MQTT broker port")
    parser.add_argument("--interval", type=float, default=1.0, help="Publish interval seconds")
    args = parser.parse_args()

    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="dietapp-health-simulator")
    client.connect(args.host, args.port, 60)
    client.loop_start()

    print("Simulator started")
    print(f"Broker: mqtt://{args.host}:{args.port}")
    print("Topics:")
    print("  health/live/heart_rate")
    print("  health/live/blood_pressure")
    print("  health/live/blood_glucose")
    print("  health/live/exercise")
    print("  health/live/all")

    try:
        while True:
            heart_rate = generate_heart_rate()
            blood_pressure = generate_blood_pressure()
            blood_glucose = generate_blood_glucose()
            exercise = generate_exercise()

            combined = {
                "ts": int(time.time() * 1000),
                "time": now_iso(),
                "heart_rate": heart_rate,
                "blood_pressure": blood_pressure,
                "blood_glucose": blood_glucose,
                "exercise": exercise,
            }

            publish(client, "health/live/heart_rate", heart_rate)
            publish(client, "health/live/blood_pressure", blood_pressure)
            publish(client, "health/live/blood_glucose", blood_glucose)
            publish(client, "health/live/exercise", exercise)
            publish(client, "health/live/all", combined)

            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("Simulator stopped")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
