package java2hu.allstar.enemies.day1;

import java2hu.Game;
import java2hu.J2hGame;
import java2hu.J2hGame.ClearType;
import java2hu.Loader;
import java2hu.Position;
import java2hu.StartupLoopAnimation;
import java2hu.allstar.AllStarStageScheme;
import java2hu.allstar.enemies.AllStarBoss;
import java2hu.allstar.util.AllStarUtil;
import java2hu.background.Background;
import java2hu.background.BackgroundBossAura;
import java2hu.gameflow.GameFlowScheme.WaitConditioner;
import java2hu.object.DrawObject;
import java2hu.object.bullet.Bullet;
import java2hu.object.player.Player;
import java2hu.object.ui.CircleHealthBar;
import java2hu.overwrite.J2hMusic;
import java2hu.spellcard.BossSpellcard;
import java2hu.system.SaveableObject;
import java2hu.touhou.bullet.ThBullet;
import java2hu.touhou.bullet.ThBulletColor;
import java2hu.touhou.bullet.ThBulletType;
import java2hu.touhou.sounds.TouhouSounds;
import java2hu.util.BossUtil;
import java2hu.util.ImageSplitter;
import java2hu.util.SchemeUtil;
import java2hu.util.Setter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Doremy extends AllStarBoss
{
	public final static String FULL_NAME = "Doremy Sweet";
	public final static String DATA_NAME = "doremy";
	public final static FileHandle FOLDER = Gdx.files.internal("enemy/" + DATA_NAME + "/");
	
	/**
	 * Spell Card Name
	 */
	final static String SPELLCARD_NAME = "Dream Path - \"Ambiguous Path\"";
	
	private Setter<BackgroundBossAura> backgroundSpawner;
	
	public Doremy(float maxHealth, float x, float y)
	{
		super(maxHealth, x, y);
		
		int chunkHeight = 192;
		int chunkWidth = 128;

		Texture sprite = Loader.texture(FOLDER.child("anm.png"));
		sprite.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.Nearest);
		
		Sprite fbs = new Sprite(Loader.texture(FOLDER.child("fbs.png")));
		fbs.setScale(2F);

		TextureRegion nameTag = new TextureRegion(Loader.texture(FOLDER.child("nametag.png")));

		Animation idle = ImageSplitter.getAnimationFromSprite(sprite, chunkHeight, chunkWidth, 8F, 1,2,3,4,3,2,1);
		idle.setPlayMode(PlayMode.LOOP);
		
		Animation left = ImageSplitter.getAnimationFromSprite(sprite, chunkHeight, chunkWidth, 8F, 13,14,15,16,17,18);
		Animation right = ImageSplitter.getAnimationFromSprite(sprite, chunkHeight, chunkWidth, 8F, 7,8,9,10,11,12);

		Animation special = new StartupLoopAnimation(ImageSplitter.getAnimationFromSprite(sprite, chunkHeight, chunkWidth, 8F, 19,20,21), ImageSplitter.getAnimationFromSprite(sprite, chunkHeight, chunkWidth, 8F, 22,23,24,23,22), 6f);
		special.setPlayMode(PlayMode.NORMAL);
	
		Music bgm = new J2hMusic(Gdx.audio.newMusic(FOLDER.child("bgm.mp3")));
		bgm.setLooping(true);
		setBgmPosition(107.18f);
		
		setColor(new Color(238 / 255f, 136 / 255f, 204 / 255f, 1.0f));
		
		set(nameTag, bgm);
		set(fbs, idle, left, right, special);
		
		backgroundSpawner = new Setter<BackgroundBossAura>()
		{
			@Override
			public void set(BackgroundBossAura t)
			{
				Background bg = new Background(Loader.texture(FOLDER.child("bg.png")))
				{
					@Override
					public void onDraw()
					{
//						Game.getGame().batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_COLOR);
						
						super.onDraw();
						
//						Game.getGame().batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
					}
				};
				
				bg.setFrameBuffer(t.getBackgroundBuffer());
				bg.setVelV(0.05d);
				bg.getSprite().setAlpha(1f);
				
				game.spawn(bg);
				
				Background bg2 = new Background(Loader.texture(FOLDER.child("bg.png")))
				{
					@Override
					public void onDraw()
					{
						Game.getGame().batch.setBlendFunction(GL20.GL_SRC_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR);
						
						super.onDraw();
						
						Game.getGame().batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
					}
				};
				
				bg2.setFrameBuffer(t.getBackgroundBuffer());
				bg2.setVelV(-0.05d);
				bg2.getSprite().setAlpha(1f);
				bg2.getSprite().setFlip(false, true);
				bg2.setZIndex(bg.getZIndex() + 4);
				
				game.spawn(bg2);
				
				final Texture bgeTexture = Loader.texture(FOLDER.child("bge.png"));
				
				class Property
				{
					private Property(Position pos, float scale)
					{
						this.pos = pos;
						this.scale = scale;
					}
					
					public Position pos;
					public float scale;
				}
				
				Property[] prop = { new Property(new Position(0, game.getMaxY() - 100), 3f), new Property(new Position(game.getMaxX() - 100, 0), 5f) };
				
				for(final Property p : prop)
				{
					DrawObject obj = new DrawObject()
					{
						Sprite sprite = new Sprite(bgeTexture);

						{
							addDisposable(bgeTexture);

							sprite.setScale(p.scale);
							sprite.setAlpha(0.2f);

							sprite.setOriginCenter();

							sprite.setPosition(p.pos.getX(), p.pos.getY());
						}


						@Override
						public void onDraw()
						{
							sprite.draw(Game.getGame().batch);
						}

						@Override
						public void onUpdateDelta(float delta)
						{
							sprite.rotate(30f * delta);
						}
					};

					obj.setFrameBuffer(t.getBackgroundBuffer());
					obj.setZIndex(bg2.getZIndex() + 2);

					game.spawn(obj);
				}
			}
		};
	}
	
	@Override
	public void onUpdate(long tick)
	{
		super.onUpdate(tick);
	}
	
	@Override
	public void onDraw()
	{
		super.onDraw();
	}
	
	public void triangle(double dirDeg, int rows, double speed, double spreadSpeed)
	{
		double rad = Math.toRadians(dirDeg - 90);
		
		for(int row = 1; row <= rows; row++)
		{
			int bullets = row;
			
			double offset = bullets / 2d;
			
			for(int i = 0; i < bullets; i++)
			{
				double number = (i - offset);
				
				double x = Math.cos(rad) * (number * spreadSpeed);
				double y = Math.sin(rad) * (number * spreadSpeed);
				
				final Bullet bullet = new Bullet(new ThBullet(ThBulletType.BULLET, ThBulletColor.CYAN), (float)(this.getX() + x), (float)(this.getY() + y));
				bullet.setDirectionDeg((float)(dirDeg + (spreadSpeed * (number / 4d))), (float)speed);
				bullet.setRotationFromVelocity(-90);
				
				game.addTaskGame(new Runnable()
				{
					@Override
					public void run()
					{
						game.spawn(bullet);
					}
				}, (int) ((row) * (1600d / speed)));
			}
		}
	}

	@Override
	public void executeFight(final AllStarStageScheme scheme)
	{
		final J2hGame g = Game.getGame();
		final Doremy boss = this;
		
		final SaveableObject<CircleHealthBar> bar = new SaveableObject<CircleHealthBar>();
		
		Game.getGame().addTaskGame(new Runnable()
		{
			@Override
			public void run()
			{
				BossUtil.cloudEntrance(boss, 60);

				g.addTaskGame(new Runnable()
				{
					@Override
					public void run()
					{
						bar.setObject(new CircleHealthBar(boss));
						
						g.spawn(boss);
						g.spawn(bar.getObject());
						
						bar.getObject().addSplit(0.8f);
						
						AllStarUtil.introduce(boss);
						
						boss.healUp();
						BossUtil.addBossEffects(boss, color);
						
						Game.getGame().startSpellCard(new NonSpell(boss));
					}
				}, 60);
			}
		}, 1);

		scheme.wait(new WaitConditioner()
		{
			@Override
			public boolean returnTrueToWait()
			{
				return !boss.isOnStage();
			}
		});

		SchemeUtil.waitForDeath(scheme, boss);
		
		bar.getObject().split();
		boss.setHealth(boss.getMaxHealth());

		Game.getGame().addTaskGame(new Runnable()
		{
			@Override
			public void run()
			{
				game.clear(ClearType.ALL);
				
				backgroundSpawner.set(scheme.getBossAura());
				
				AllStarUtil.presentSpellCard(boss, SPELLCARD_NAME);
				
				Game.getGame().startSpellCard(new Spell(boss));
			}
		}, 1);
		
		SchemeUtil.waitForDeath(scheme, boss);
		
		scheme.doWait();
		
		Game.getGame().addTaskGame(new Runnable()
		{
			@Override
			public void run()
			{
				Game.getGame().delete(boss);
				
				Game.getGame().clear(ClearType.ALL);
				
				BossUtil.mapleExplosion(boss.getX(), boss.getY());
			}
		}, 1);
		
		scheme.waitTicks(5); // Prevent concurrency issues.
	}
	
	public static class NonSpell extends BossSpellcard<Doremy>
	{	
		public NonSpell(Doremy owner)
		{
			super(owner);
			
			owner.setDamageModifier(0.75f);
		}
		
		private long rotationTick;

		@Override
		public void tick(int tick, final J2hGame game, final Doremy boss)
		{
			final Player player = game.getPlayer();
			
			final int waitPeriod = 160;
			
			if(tick == 100)
			{
				BossUtil.charge(boss, boss.getColor(), false);
				TouhouSounds.Enemy.ACTIVATE_1.play();
			}
			
			if(tick == 130)
			{
				boss.playSpecial(true);
			}
			
			if(tick == 150)
			{
				BossUtil.chargeExplosion(boss, boss.getColor());
			}
			
			if(tick < waitPeriod)
			{
				return;
			}
			
			tick -= waitPeriod;
			
			if(tick % 900 < 700)
			{
				if(tick % 900 == 0)
				{
					TouhouSounds.Enemy.EXPLOSION_3.play();
				}
				
				rotationTick++;
				
				if(rotationTick % 3 == 0)
				{
					TouhouSounds.Enemy.RELEASE_1.play(0.3f);
				}

				if(rotationTick % 6 == 0)
				{
					final float fullRotationTicks = 700f;

					float rotationMul = ((rotationTick % fullRotationTicks / fullRotationTicks)) * 4f;

					ThBulletColor color = ThBulletColor.PURPLE;

					if(rotationMul > 2f)
					{
						rotationMul = 4f - rotationMul;
						color = ThBulletColor.BLUE;
					}

					final float offsetAngle = rotationMul * 360f * 2f;

					final float rows = 4f;

					for(float angle = 0; angle <= 360; angle += (360/rows))
					{
						final float directions = 4f;

						for(float subAngle = 0; subAngle < 360; subAngle += (360/directions))
						{
							final float finalAngle = angle + offsetAngle;
							final float finalRad = (float) Math.toRadians(finalAngle);

							float finalAngleMul = offsetAngle / 360f / 2f;

							Bullet bullet = new Bullet(new ThBullet(ThBulletType.ORB_LARGE, color), (float) (boss.getX() + (Math.cos(finalRad) * (50 + (100 * rotationMul)))), (float) (boss.getY() + (Math.sin(finalRad) * (50 + (100 * rotationMul)))));

							bullet.setZIndex((int) (bullet.getZIndex() + rotationTick));
							
							bullet.setScale(0.5f);
							
							float speed = (200f + (200f * rotationMul));

							bullet.getSpawnAnimationSettings().setTime(10f + (1/speed * 20f));

							final float f = finalAngleMul * 180;

							bullet.setDirectionDeg((subAngle) + f, speed);

							game.spawn(bullet);
						}
					}
				}
			}
			else if (tick % 900 == 760)
			{
				float width = 400;
				
				int minX = (int) Math.max(game.getMinX() + 300, Math.min(game.getMaxX() - width - 300, boss.getX() - 200));
				
				int minY = (int) (game.getCenterY() + 200);
				
				Rectangle box = new Rectangle(minX, minY, width, 200);
				
				BossUtil.moveAroundRandomly(boss, box, 800);
			}
			else
			{
				if(tick % 900 < 880)
					boss.playSpecial(false);
				else
				{
					boss.playSpecial(true);
				}
				
				rotationTick = 0;
			}
		}
	}

	public static class Spell extends BossSpellcard<Doremy>
	{
		public Spell(Doremy owner)
		{
			super(owner);
			
			owner.setDamageModifier(0.5f);
		}

		@Override
		public void tick(int tick, J2hGame game, Doremy boss)
		{
			final Player player = game.getPlayer();
			
			final int waitPeriod = 160;
			
			if(tick == 0)
			{
				boss.playSpecial(false);
				BossUtil.moveTo(boss, game.getCenterX(), game.getMaxY() - 200, 1000);
			}
			
			if(tick == 130)
			{
				boss.playSpecial(true);
			}
			
			if(tick == 150)
			{
				BossUtil.chargeExplosion(boss, boss.getColor());
			}
			
			if(tick < waitPeriod)
			{
				return;
			}
			
			tick -= waitPeriod;
			
			if(tick % 20 <= 13)
			{
				if(tick % 3 == 0)
				{
					TouhouSounds.Enemy.RELEASE_1.play(0.3f);
				}
				
				for(ThBullet bulletType : new ThBullet[] { new ThBullet(ThBulletType.BALL_BIG, ThBulletColor.PURPLE), new ThBullet(ThBulletType.DOT_SMALL_MOON, ThBulletColor.BLUE) })
				for(float startAngle = 0; startAngle < 360; startAngle += 60)
				{
					boolean dot = bulletType.getType() == ThBulletType.DOT_SMALL_MOON;
					
					if(dot && tick % 4 != 0)
						continue;
					
					final double speed = dot ? 200 : 100;
					float finalStartAngle = (float) (startAngle + (tick % speed / speed) * 360);
					
					Bullet bullet = new Bullet(bulletType, boss.getX(), boss.getY());

					bullet.setZIndex(bullet.getZIndex() + (20 - (tick % 20)));
					
					bullet.setScale(0.9f);
					
					final float cos = (float) Math.cos(((tick % 60) / 60d) * 2 * Math.PI);
					
					bullet.getSpawnAnimationSettings().setTime(60 + (-20 * cos));

					bullet.setDirectionDeg(finalStartAngle + 50 * cos, (dot ? 40f : 300f) + (200f * cos));
					
					game.spawn(bullet);
				}
				
			}
		}
	}
}
