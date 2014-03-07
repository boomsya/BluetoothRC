
#include <Servo.h>

Servo servoSteering;
const int servoSignalPin = 7; // servo yellow(signal) pin

const int servoMiddle = 88; // servo middle position
const int servoMaxAngle = 35; // servo max angle (degrees)
const int servoLeftBound = servoMiddle - servoMaxAngle;
const int servoRightBound = servoMiddle + servoMaxAngle;

int servoPos = servoMiddle;
int servoTrim = 0;

const int dcMotorA1Pin = 11; // dc motor A+ pin
const int dcMotorA2Pin = 12; // dc motor A- pin
const int dcMotorPWMPin = 3; // dc motor PWM pin

const int ledPin = 13; // data received pin

const int dcMotorPWMLowerBound = 128; // dc motor min PWM (min speed)
const int dcMotorPWMHighBound = 255; // dc motor max PWM (max speed)

int dcMotorDir = 1; // 1 - forward, -1 - backward
int dcMotorSpd = 0; // 0 - stand still, value between dcMotorPWMLowerBound and dcMotorPWMHighBound to move

const int cmdTimeout = 2000; // max time between commands from rc control, will stop if no new commands arrive

typedef enum { NONE = 0, SERVO_L, SERVO_R, TRIM, DCMOTOR_F, DCMOTOR_B, STOP } mode_t;
mode_t opMode = NONE;

#define IS_DIGIT(x) ('9' >= (x) && '0' <= (x))

unsigned long lastCmd = 0;

void setup() {

  Serial.begin(9600);

// https://github.com/arduino/Arduino/issues/672
//  Serial.setBlocking(0);

  servoSteering.attach(servoSignalPin);

  pinMode(dcMotorA1Pin, OUTPUT);
  pinMode(dcMotorA2Pin, OUTPUT);
  pinMode(dcMotorPWMPin, OUTPUT);

  pinMode(ledPin, OUTPUT);

  digitalWrite(dcMotorA1Pin, 0);
  digitalWrite(dcMotorA2Pin, 0);
  analogWrite(dcMotorPWMPin, 0);
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
          opMode = DCMOTOR_F;
          break;
        case 'B':
        case 'b': // dc motor move back mode
          opMode = DCMOTOR_B;
          break;
        case 'S':
        case 's': // full stop mode
          servoPos = servoMiddle + servoTrim; // center front wheels
          dcMotorDir = 1; // 1st gear
          dcMotorSpd = 0; // neutral on
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
        case DCMOTOR_F:
          dcMotorDir = 1;
          dcMotorSpd = ch == '0' ? 0 : dcMotorPWMLowerBound + (dcMotorPWMHighBound - dcMotorPWMLowerBound) * (ch - '0') / 10;
          break;
        case DCMOTOR_B:
          dcMotorDir = -1;
          dcMotorSpd = ch == '0' ? 0 : dcMotorPWMLowerBound + (dcMotorPWMHighBound - dcMotorPWMLowerBound) * (ch - '0') / 10;
          break;
        default:
          break;
      }
      
      opMode = NONE;
    }

    if (dcMotorDir == 1)
      Serial.print("F ");
    else
      Serial.print("R ");

    Serial.print(dcMotorSpd);
    Serial.print(" S ");
    Serial.print(servoPos);
    Serial.print(" T ");
    Serial.println(servoTrim);

    digitalWrite(dcMotorA1Pin, dcMotorDir == 1 ? 1 : 0);
    digitalWrite(dcMotorA2Pin, dcMotorDir == 1 ? 0 : 1);
    analogWrite(dcMotorPWMPin, dcMotorSpd);

    servoSteering.write(servoPos);
  }
  
  if (dcMotorSpd > 0 && (lastCmd  + cmdTimeout) < millis()) {

    // rc control doesn't send anything, out of range or hang up, do full stop
    dcMotorDir = 1; // 1st gear
    dcMotorSpd = 0; // neutral on

    digitalWrite(dcMotorA1Pin, 1);
    digitalWrite(dcMotorA2Pin, 0);
    analogWrite(dcMotorPWMPin, 0);

    servoSteering.write(servoPos);
  }

  digitalWrite(ledPin, LOW);
}