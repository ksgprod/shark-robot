package ia;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.*;

public class SharkRobot extends AdvancedRobot {
	boolean movingForward; // � definida como true quando setAhead � chamada e vice-versa
	boolean inWall; // � verdade quando rob� est� perto da parede.

	public void run() {

		setColors();

		// Cada parte do rob� move-se livremente dos outros.
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		// Verifique se o rob� est� mais perto do que 50px da parede.
		if (getX() <= 50 || getY() <= 50
				|| getBattleFieldWidth() - getX() <= 50
				|| getBattleFieldHeight() - getY() <= 50) {
			this.inWall = true;
		} else {
			this.inWall = false;
		}

		setAhead(40000); // v� em frente at� inverter o sentido
		setTurnRadarRight(360); // scannear at� encontrar seu primeiro inimigo
		this.movingForward = true; // chamamos setAhead, ent�o movingForward � verdade

		while (true) {
			// Verifica se estamos perto da parede e se j� verificamos positivo.
			// Caso n�o verificamos, inverte a dire��o e seta flag
			// para true.
			if (getX() > 50 && getY() > 50
					&& getBattleFieldWidth() - getX() > 50
					&& getBattleFieldHeight() - getY() > 50
					&& this.inWall == true) {
				this.inWall = false;
			}
			if (getX() <= 50 || getY() <= 50
					|| getBattleFieldWidth() - getX() <= 50
					|| getBattleFieldHeight() - getY() <= 50) {
				if (this.inWall == false) {
					reverseDirection();
					inWall = true;
				}
			}

			// Se o radar parou de girar, procure um inimigo
			if (getRadarTurnRemaining() == 0.0) {
				setTurnRadarRight(360);
			}

			execute(); // executar todas as a��es set.
		}
	}

	public void onHitWall(HitWallEvent e) {
		reverseDirection();
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		// Calcular a posi��o exata do rob�
		double absoluteBearing = getHeading() + e.getBearing();

		// vire s� o necess�rio e nunca mais do que uma volta...
		// vendo-se o angulo que fazemos com o robo alvo e descontando
		// o Heading e o Heading do Radar pra ficar com o angulo
		// correto, normalmente.
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing
				- getGunHeading());
		double bearingFromRadar = normalRelativeAngleDegrees(absoluteBearing
				- getRadarHeading());

		// giro para realizar movimento espiral ao inimigo
		// (90 levaria ao paralelo)
		if (this.movingForward) {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
		} else {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 100));
		}

		// Se perto o suficiente, fogo!
		if (Math.abs(bearingFromGun) <= 4) {
			setTurnGunRight(bearingFromGun); // mantem o canh�o centrado sobre o inimigo
			setTurnRadarRight(bearingFromRadar); // mantem o radar centrado sobre o inimigo

			// Quanto mais precisamente objetivo, maior ser� a bala.
			// N�o dispare nos a defici�ncia, sempre salvar 0,1
			if (getGunHeat() == 0 && getEnergy() > .2) {
				fire(Math.min(
						4.5 - Math.abs(bearingFromGun) / 2 - e.getDistance() / 250, 
						getEnergy() - .1));
			}
		} // caso contr�rio, basta definir a arma para virar.
		else {
			setTurnGunRight(bearingFromGun);
			setTurnRadarRight(bearingFromRadar);
		}

		// se o radar n�o estiver girando, gera evento de giro (scanner)
		if (bearingFromGun == 0) {
			scan();
		}
	}

	// em contato com o robo, se tenha sido por nossa conta, inverte a dire��o
	public void onHitRobot(HitRobotEvent e) {
		if (e.isMyFault()) {
			reverseDirection();
		}
	}

	private void setColors() {
		setBodyColor(Color.BLACK);
		setGunColor(Color.BLACK);
		setRadarColor(Color.GRAY);
		setBulletColor(Color.GRAY);
		setScanColor(Color.GRAY);
	}

	// mudar de frente para tr�s e vice-versa
	public void reverseDirection() {
		if (this.movingForward) {
			setBack(40000);
			this.movingForward = false;
		} else {
			setAhead(40000);
			this.movingForward = true;
		}
	}
}