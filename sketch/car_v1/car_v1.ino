
#include <Servo.h>
#include <Stepper.h>

Servo servoSteering;
const int servoSignalPin = 7; // servo yellow(signal) pin

const int servoMiddle = 88; // servo middle position
const int servoMaxAngle = 35; // servo max angle (degrees)
const int servoLeftBound = servoMiddle - servoMaxAngle;
const int servoRightBound = servoMiddle + servoMaxAngle;

int servoPos = servoMiddle;
int servoTrim = 0;

const int ledPin = 13;

const int stepperPin1 = 11;
const int stepperPin2 = 12;
const int stepperPin3 = 3;
const int stepperPin4 = 4;

const float motorSteps = 360 / 3.75;

Stepper myStepper(motorSteps, stepperPin1, stepperPin2, stepperPin3, stepperPin4);

const int stepperLowerBound = 20; // dc motor min PWM (min speed)
const int stepperHighBound = 200; // dc motor max PWM (max speed)

int stepperDir = 1; // 1 - forward, -1 - backward
int stepperSpd = 0; // 0 - stand still, value between stepperLowerBound and stepperHighBound to move

const int cmdTimeout = 2000; // max time between commands from rc control, will stop if no new commands arrive

typedef enum { NONE = 0, SERVO_L, SERVO_R, TRIM, stepper_F, stepper_B, STOP } mode_t;
mode_t opMode = NONE;

#define IS_DIGIT(x) ('9' >= (x) && '0' <= (x))

unsigned long lastCmd = 0;

void setup() {

  Serial.begin(9600);

  servoSteering.attach(servoSignalPin);

  myStepper.setSpeed(10);
  pinMode(ledPin, OUTPUT);
}

void loop() {

  while (Serial.available()) {

    digitalWrite(ledPin, HIGH);

    int ch = Serial.read();
    lastCmd = millis(); // save command receive time for later

    if (opMode == NONE) {
      switch (ch) {
        case 'T':
        case 't': // trim mode
          opMode = TRIM;
          break;
        case 'L':
        case 'l': // servo left mode
          opMode = SERVO_L;
          break;
        case 'R':
        case 'r': // servo right mode
          opMode = SERVO_R;
          break;
        case 'F':
        case 'f': // dc motor move front mode
          opMode = stepper_F;
          break;
        case 'B':
        case 'b': // dc motor move back mode
          opMode = stepper_B;
          break;
        case 'S':
        case 's': // full stop mode
          servoPos = servoMiddle + servoTrim; // center front wheels
          stepperDir = 1; // 1st gear
          stepperSpd = 0; // neutral on
        default:
          opMode = NONE;
          break;
      }
    } else if (IS_DIGIT(ch)) {
      switch (opMode) {
        case TRIM:
          servoTrim = '5' - ch;
          break;
        case SERVO_L:
          servoPos = servoMiddle + servoTrim - (ch - '0') * servoMaxAngle / 10;
          if (servoPos < servoLeftBound) servoPos = servoLeftBound;
          break;
        case SERVO_R:
          servoPos = servoMiddle + servoTrim + (ch - '0') * servoMaxAngle / 10;
          if (servoPos > servoRightBound) servoPos = servoRightBound;
          break;
        case stepper_F:
          stepperDir = 1;
          stepperSpd = ch == '0' ? 0 : stepperLowerBound + (stepperHighBound - stepperLowerBound) * (ch - '0') / 10;
          break;
        case stepper_B:
          stepperDir = -1;
          stepperSpd = ch == '0' ? 0 : stepperLowerBound + (stepperHighBound - stepperLowerBound) * (ch - '0') / 10;
          break;
        default:
          break;
      }
      
      opMode = NONE;
    }

    if (stepperDir == 1)
      Serial.print("F ");
    else
      Serial.print("R ");

    Serial.print(stepperSpd);
    Serial.print(" S ");
    Serial.print(servoPos);
    Serial.print(" T ");
    Serial.println(servoTrim);

    servoSteering.write(servoPos);
  }
  
  if (stepperSpd > 0 && (lastCmd  + cmdTimeout) < millis()) {

    // rc control doesn't send anything, out of range or hang up, do full stop
    stepperDir = 1; // 1st gear
    stepperSpd = 0; // neutral on

    servoSteering.write(servoPos);
  }

   if (stepperSpd > 0) {
      myStepper.setSpeed(stepperSpd);
      myStepper.step(1 * stepperDir);
    }

  digitalWrite(ledPin, LOW);
}
