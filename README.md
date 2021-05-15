# gameai-final-project

OUTLINE:
  1) HOW TO RUN THE GAME/WHERE THE FILES ARE
  2) COMMENTS/ISSUES


1) HOW TO RUN THE GAME/WHERE OUR FILES ARE
    - To change the game level use GVGAI/src/tracks/singlePlayer/Test.java
    - The "int levelIndex" on line 61 can be changed from 0-4, but we reccommend using 0, 1, or 2 because of how the framework
      returned enemy positions based on the different sprites.
    - Our files are in the FSMsingle package in GVGAI/src/FSMsingle
    - We made the State.java, AgentStates.java, and FSMAgent.java files.

2) COMMENTS/ISSUES
    - The framework caused a lot of issues for us later in the project.
    - A lot of functions in StateObservation either didn't return anything or returned something different than what we thought they would.
    - For example, GetNPCPositions (which we used to track the enemies), only returns one enemy per each individual enemy sprite.
    - On level 0, even though there are 3 spider enemies, the function only return one enemy location.
    - We also couldn't get the wall positions for any level. We thought they would be in getImmovablePositions, but we couldn't find a function that gave us the wall positions.
    - We compensated for this by adding in a function "oof" that moves randomly if the last move doesn't change the agent's position.
    - One thing that caused major issues and a lot of wasted time was the AStar package.
    - We tried for a while to implement AStar into our agent because that would've allowed us to create a better vision range for the agent and make the agent perform much better in the levels.
    - We also couldn't make our own AStar because of the cryptic nature of the framework and how the sprite "itypes" worked in the game.
    - Eventually we decided to still use the vision range of 3 for enemies but had to give the agent positions to go to.
