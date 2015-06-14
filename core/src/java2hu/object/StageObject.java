package java2hu.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java2hu.Game;
import java2hu.IPosition;
import java2hu.J2hGame;
import java2hu.events.EventListener;
import java2hu.object.bullet.Bullet;
import java2hu.overwrite.J2hObject;
import java2hu.pathing.PathingHelper;
import java2hu.plugin.Plugin;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.sun.istack.internal.Nullable;

public abstract class StageObject extends J2hObject implements IPosition
{
	private ArrayList<Plugin> effects = new ArrayList<Plugin>();
	
	public ArrayList<Plugin> getEffects()
	{
		return effects;
	}
	
	public void addEffect(Plugin effect)
	{
		effects.add(effect);
	}
	
	protected ArrayList<Disposable> disposables = new ArrayList<Disposable>();
	
	protected float x;
	protected float y;
	protected float lastX;
	protected float lastY;
	protected long lastMoveTime;
	
	protected long createTick;
	
	protected int zIndex = 0;
	
	protected String name;
	
	public StageObject(float x, float y)
	{
		this.x = x;
		this.y = y;
		this.lastY = y;
		this.lastX = x;
	}
	
	public void setX(float x)
	{
		if(!Game.getGame().inBoundary(x, y))
		{
			return;
		}
		
		this.lastX = this.x;
		this.x = x;
		this.lastMoveTime = System.currentTimeMillis();
	}
	
	public void setY(float y)
	{
		if(!Game.getGame().inBoundary(x, y))
		{
			return;
		}
		
		this.lastY = this.y;
		this.y = y;
		this.lastMoveTime = System.currentTimeMillis();
	}
	
	public void setPosition(IPosition pos)
	{
		setPosition(pos.getX(), pos.getY());
	}
	
	public void setPosition(float x, float y)
	{
		setX(x);
		setY(y);
	}
	
	@Override
	public float getX()
	{
		return x;
	}
	
	@Override
	public float getY()
	{
		return y;
	}
	
	/**
	 * Name is used for better profiling (Or whatever you want to use it for)
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Name is used for better profiling (Or whatever you want to use it for)
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name != null ? name : super.toString();
	}
	
	public long getLastMoveTime()
	{
		return lastMoveTime;
	}
	
	public void onSpawn()
	{
		this.createTick = Game.getGame().getActiveTick();
		gameTick = Game.getGame().getActiveTick() == Game.getGame().getTick();
	}
	
	public void delete()
	{
		if(getOwnedBy() != null)
			return;
		
		onDelete();
	}
	
	public void onDelete()
	{
		disposeAll();
	}
	
	public void disposeAll()
	{
		disposeDisposables();
		
		disposeChildren();
	}
	
	/**
	 * Disposes of all children by deleting them.
	 */
	public void disposeChildren()
	{
		Game.getGame().runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				for(StageObject obj : children)
				{
					game.delete(obj);
				}
				
				children.clear();
			}
		});
	}
	
	/**
	 * Disposes all the disposables of this object.
	 */
	public void disposeDisposables()
	{
		Game.getGame().runAsync(new Runnable()
		{
			@Override
			public void run()
			{ 
				Iterator<Disposable> it = disposables.iterator();
				
				while(it.hasNext())
				{
					Disposable disp = it.next();
					
					try
					{
						disp.dispose();
						
						it.remove();
					}
					catch(Exception e)
					{
						// If an error has presented itself, don't remove it from the list and dispose of this in the main thread instead.
					}
				}
				
				Game.getGame().addTask(new Runnable()
				{
					@Override
					public void run()
					{
						for(Disposable disp : disposables)
						{
							disp.dispose();
						}
					}
				}, 0);
			}
		});
	}
	
	public void addDisposable(TextureRegion disp)
	{
		if(disp == null)
			return;
		
		addDisposable(disp.getTexture());
	}
	
	public void addDisposable(Animation disp)
	{
		if(disp == null)
			return;
		
		for(TextureRegion r : disp.getKeyFrames())
			addDisposable(r);
	}
	
	/**
	 * Unregisters a listener once this object is deleted.
	 * @param listener
	 */
	public void addDisposable(final EventListener listener)
	{
		Disposable disp = new Disposable()
		{
			@Override
			public void dispose()
			{
				Game.getGame().unregisterEvents(listener);
			}
		};
		
		addDisposable(disp);
	}
	
	public void addDisposable(Disposable disp)
	{
		if(disp == null)
			return;
		
		disposables.add(disp);
	}
	
	public abstract float getWidth();
	public abstract float getHeight();
	
	public float getLastX()
	{
		return lastX;
	}
	
	public float getLastY()
	{
		return lastY;
	}
	
	boolean gameTick = true;
	
	public long getTicksAlive()
	{
		return (gameTick ? Game.getGame().getTick() : Game.getGame().getPauseTick()) - createTick;
	}
	
	public int getZIndex()
	{
		return zIndex;
	}
	
	public void setZIndex(int zIndex)
	{
		this.zIndex = zIndex;
	}
	
	/**
	 * A child object is an object which will be deleted once it's parent is deleted, therefore rendering them a "managed" object.
	 * Child status is gained by being added to the list of children, parent status is having objects in your childrens list.
	 * So you can only influence that by removing or adding children, you can't remove or add a parent to an object.
	 */
	private ArrayList<StageObject> children = new ArrayList<StageObject>();
	
	/**
	 * Returns a read only list of the children of this object.
	 */
	public ArrayList<StageObject> getChildren()
	{
		return new ArrayList<StageObject>(children);
	}
	
	/**
	 * Returns a read only list of all active stage objects that have this object as a child.
	 */
	public ArrayList<StageObject> getParents()
	{
		ArrayList<StageObject> list = new ArrayList<StageObject>();
		
		for(StageObject obj : game.getStageObjects())
		{
			if(obj.isChild(this))
			{
				list.add(obj);
			}
		}
		
		for(Bullet obj : game.getBullets())
		{
			if(obj.isChild(this))
			{
				list.add(obj);
			}
		}
		
		return list;
	}
	
	/**
	 * Adds the specified object as a child for this object.
	 * See {@link #children} for working.
	 */
	public void addChild(StageObject obj)
	{
		children.add(obj);
	}
	
	/**
	 * Removes the specified object as a child for this object.
	 * See {@link #children} for working.
	 */
	public boolean removeChild(StageObject obj)
	{
		return children.remove(obj);
	}
	
	/**
	 * Adds this object as a child to the specified object.
	 * See {@link #children} for working.
	 */
	public void addParent(StageObject obj)
	{
		obj.addChild(this);
	}
	
	/**
	 * Removes this object as a child to the specified object.
	 * See {@link #children} for working.
	 */
	public boolean removeParent(StageObject obj)
	{
		return obj.removeChild(this);
	}
	
	/**
	 * Returns if the specified object is a child to this object.
	 * See {@link #children} for working.
	 */
	public boolean isChild(StageObject obj)
	{
		return children.contains(obj);
	}
	
	/**
	 * Returns if the specified object is a parent of this object.
	 * See {@link #children} for working.
	 */
	public boolean isParent(StageObject obj)
	{
		return obj.children.contains(this);
	}
	
	/**
	 * If this object remains on the stage when it gets cleared.
	 * @return
	 */
	public boolean isPersistant()
	{
		return false;
	}
	
	/**
	 * If this object will get updated while being paused.
	 * @return
	 */
	public boolean isActiveDuringPause()
	{
		return false;
	}
	
	private StageObject ownedBy;
	
	/**
	 * Sets the object this one is owned by, which will make some methods return the status of the owner instead.
	 * @param ownedBy
	 */
	public void setOwnedBy(StageObject ownedBy)
	{
		this.ownedBy = ownedBy;
	}
	
	/**
	 * Null if unowned.
	 */
	public StageObject getOwnedBy()
	{
		return ownedBy;
	}
	
	/**
	 * Returns if this object is on stage.
	 * Or, if this object is owned, returns if it's owner is on stage.
	 */
	public boolean isOnStage()
	{
		if(ownedBy != null)
		{
			return ownedBy.isOnStage();
		}
		
		return isOnStageRaw();
	}
	
	/**
	 * Returns if this object is on the stage.
	 */
	public boolean isOnStageRaw()
	{
		J2hGame g = Game.getGame();
		
		if(g.getStageObjects().contains(this))
		{
			return true;
		}
		else if(g.getBullets().contains(this))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	protected PathingHelper pathing = new PathingHelper();
	
	public PathingHelper getPathing()
	{
		return pathing;
	}
	
	protected ShaderProgram shader = null;
	
	/**
	 * Set object to a specific shader.
	 * Note: Not pooling items like this together (with z index), or using a lot of different shaders, caused a TON of lag.
	 * @param shader
	 */
	public void setShader(@Nullable ShaderProgram shader)
	{
		this.shader = shader;
	}
	
	public ShaderProgram getShader()
	{
		return shader;
	}
	
	public void clearShader()
	{
		setShader(null);
	}
	
	protected FrameBuffer buffer = null;
	
	/**
	 * Set object to use a specific framebuffer
	 * Note: We haven't tested performance impact if you don't pool framebuffer shared objects together (with z index)
	 * Drawing to a frame buffer will mean that it won't get drawn default, since I'm also new to this, what I used as guide is this:
	 * http://stackoverflow.com/questions/24480901/libgdx-overlay-texture-above-another-texture-using-shader
	 * @param shader
	 */
	public void setFrameBuffer(@Nullable FrameBuffer buffer)
	{
		this.buffer = buffer;
	}
	
	public FrameBuffer getFrameBuffer()
	{
		return buffer;
	}
	
	public void clearFrameBuffer()
	{
		setFrameBuffer(null);
	}
	
	/**
	 * A list of owned objects, any owned objects will be rendered/updated in the same draw/update calls with this object.
	 * The boolean key decides when the object will be drawn.
	 */
	private HashMap<StageObject, OwnedObjectData> ownedObjects = new HashMap<StageObject, OwnedObjectData>();
	
	public static class OwnedObjectData
	{
		/**
		 * Draw the owned object after it's owner.
		 */
		public boolean drawAfter = true;
	}
	
	/**
	 * Adds the specified object as owned by this one.
	 * This method will now call the tick/render methods for this object and it is removed from the stage.
	 * This will use the default data, which will draw after the owned object
	 */
	public void addOwnedObject(StageObject obj)
	{
		addOwnedObject(obj, new OwnedObjectData());
	}
	
	/**
	 * Adds the specified object as owned by this one.
	 * This method will now call the tick/render methods for this object and it is removed from the stage.
	 */
	public void addOwnedObject(StageObject obj, OwnedObjectData data)
	{
		ownedObjects.put(obj, data);
		obj.setOwnedBy(this);
	}
	
	public void removeOwnedObject(StageObject obj)
	{
		ownedObjects.remove(obj);
		obj.setOwnedBy(null);
	}
	
	private int SRC = GL20.GL_SRC_ALPHA;
	private int DST = GL20.GL_ONE_MINUS_SRC_ALPHA;
	
	/**
	 * Sets this objects blend mode to one that will make it appear to be glowing hot.
	 */
	public void setGlowing()
	{
		setBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
	}
	
	/**
	 * Sets this object's blend mode to the default.
	 * SRC: GL_SRC_ALPHA
	 * DST: GL_ONE_MINUS_SRC_ALPHA
	 */
	public void setBlendFuncDefault()
	{
		setBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public void setBlendFunc(int sFactor, int dFactor)
	{
		SRC = sFactor;
		DST = dFactor;
	}
	
	public int getBlendFuncSrc()
	{
		return SRC;
	}
	
	public int getBlendFuncDst()
	{
		return DST;
	}
	
	public void draw()
	{
		if(!isOnStage())
			return;
		
		if(getOwnedBy() == null)
			game.batch.setBlendFunction(SRC, DST);
		
		for(Entry<StageObject, OwnedObjectData> owned : ownedObjects.entrySet())
		{
			if(owned.getValue().drawAfter)
				continue;
			
			owned.getKey().draw();
		}
		
		onDraw();
		
		for(Entry<StageObject, OwnedObjectData> owned : ownedObjects.entrySet())
		{
			if(!owned.getValue().drawAfter)
				continue;
			
			owned.getKey().draw();
		}
		
		if(getOwnedBy() == null)
			game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	/**
	 * The draw method should only draw whatever it is on screen.
	 * This will keep drawing even when the game is paused, but updating will not proceed.
	 */
	public abstract void onDraw();
	
	public void update(long tick)
	{
		onUpdate(tick);
		
		for(Plugin effect : effects)
		{
			effect.update(this, tick);
		}
		
		getPathing().tick();
		
		for(Entry<StageObject, OwnedObjectData> owned : ownedObjects.entrySet())
		{
			owned.getKey().update(tick);
			
			if(owned.getKey().isOnStageRaw())
			{
				game.delete(owned.getKey()); // Shouldn't be drawn on it's own.
			}
		}
	}
	
	public void update(float second)
	{
		onUpdateDelta(second);
		
		for(Entry<StageObject, OwnedObjectData> owned : ownedObjects.entrySet())
		{
			owned.getKey().update(second);
		}
	}
	
	/**
	 * The update method should be used to update any logic, positioning, etc.
	 * This will halt when the game is paused.
	 * This methods runs on the logic loop, which runs at {@value J2hGame#LOGIC_TPS} tps by default, but can be different. 
	 * @param tick
	 */
	public abstract void onUpdate(long tick);
	
	/**
	 * This update method will be called every frame, with delta as the time passed since the last frame was rendered.
	 * Basically any visual things should be updated here, like movement.
	 * This is because they can be smoothed out to look really good at all framerates.
	 */
	public void onUpdateDelta(float delta)
	{
		
	}
}
