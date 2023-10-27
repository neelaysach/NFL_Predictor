import math
import random
import pandas as pd
from sklearn.tree import DecisionTreeClassifier
from sklearn.neural_network import MLPClassifier

train_data = pd.read_csv('nfl_data.csv')

test_data = pd.read_csv('nfl_data_2020s.csv')
testArr = test_data.values

# Columns:
# 'Team', 'Year', 'Week', 'Win/Loss', 'Win Loss Factor', 'Home/Away', 'Points Scored',
# 'Points Allowed', 'Third Down', 'Third Down Allowed', 'Pass Yards', 'Pass Yards Allowed',
# 'Rush Yards', 'Rush Yards Allowed'

# ADD NAMES FROM COLNAMES TO THIS TO DROP A COLUMN FROM PARAMETERS FED INTO MODEL
columnsToDrop = ['Team','Year', 'Week', 'Win/Loss', 'Opp Team']

X = train_data.drop(columns=columnsToDrop)
y = train_data['Win/Loss']
y = y.astype(int)

# Prepares the test data for the model
testDF = test_data.drop(columns=columnsToDrop)

# Creates and runs the model
model = MLPClassifier()
model.fit(X,y)
neurPred = model.predict(testDF)

# Calculates correctness percentage
print("Neural Network Results: ")
matches = 0
weekPercents = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
weekGames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
for i in range(len(testArr)):
    index = int(testArr[i][2]) - 2
    weekGames[index] += 1
    if neurPred[i] == testArr[i][3].astype(int):
        matches += 1
        weekPercents[index] += 1

for i in range(len(weekPercents)):
    weekPercents[i] /= weekGames[i]
    #print('week ' + str(i + 2) + ' percentage: ' + str(weekPercents[i]))
print(matches / len(neurPred))


#Decision Tree model
model = MLPClassifier()
model.fit(X,y)
treePred = model.predict(testDF)

# Calculates correctness percentage
print("Decision Tree Results: ")
matches = 0
weekPercents = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
weekGames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
for i in range(len(testArr)):
    index = int(testArr[i][2]) - 2
    weekGames[index] += 1
    if treePred[i] == testArr[i][3].astype(int):
        matches += 1
        weekPercents[index] += 1

#average = 0
for i in range(len(weekPercents)):
    weekPercents[i] /= weekGames[i]
    #average += weekPercents[i]
    #print('week ' + str(i + 2) + ' percentage: ' + str(weekPercents[i]))
print(matches / len(neurPred))
#print(average / 15)

#Use algorithm to predict instead of machine learning
algData = test_data.values

#Used for the weights of each data set
factorHigh = 0.791866
pointsHigh = 30
pointsAllowedHigh = 28
thirdHigh = 0.627273
thirdAllowedHigh = 0.59231
passHigh = 243
passAllowedHigh = 252
rushHigh = 184
rushAllowedHigh = 186

algPred = []
algVals = []
for i in range(len(algData)):
    val = 0
    val += (algData[i][4] / factorHigh) * 0.125
    if (algData[i][5] == 0):
        val -= 0.03
    else:
        val += 0.03
    val += (algData[i][6] / pointsHigh) * 0.06667
    val += (algData[i][7] / pointsAllowedHigh) * 0.06667
    val += (algData[i][8] / thirdHigh) * 0.125
    val += (algData[i][9] / thirdAllowedHigh) * 0.125
    val += (algData[i][10] / passHigh) * 0.06667
    val += (algData[i][11] / passAllowedHigh) * 0.06667
    val += (algData[i][12] / rushHigh) * 0.06667
    val += (algData[i][13] / rushAllowedHigh) * 0.06667
    if (val > 0):
        algPred.append(1)
    else:
        algPred.append(0)
    algVals.append(val)

# Calculates correctness percentage
print("Algorithm Results: ")
matches = 0
weekPercents = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
weekGames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
for i in range(len(algPred)):
    index = int(algData[i][2]) - 2
    weekGames[index] += 1
    if algPred[i] == testArr[i][3].astype(int):
        matches += 1
        weekPercents[index] += 1

for i in range(len(weekPercents)):
    weekPercents[i] /= weekGames[i]
    #print('week ' + str(i + 2) + ' percentage: ' + str(weekPercents[i]))
print(matches / len(neurPred))

#Combining model and algorithm
combPred = []
for i in range(len(algPred)):
    num = int(random.random()*2)
    if abs(algVals[i]) < 0.1:
        if num == 0:
            combPred.append(neurPred[i])
        else:
            combPred.append(algPred[i])
    else:
        combPred.append(algPred[i])
# Calculates correctness percentage
print("Combined Results: ")
matches = 0
weekPercents = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
weekGames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
for i in range(len(algPred)):
    index = int(algData[i][2]) - 2
    weekGames[index] += 1
    if combPred[i] == testArr[i][3].astype(int):
        matches += 1
        weekPercents[index] += 1

for i in range(len(weekPercents)):
    weekPercents[i] /= weekGames[i]
    #print('week ' + str(i + 2) + ' percentage: ' + str(weekPercents[i]))
print(matches / len(neurPred))