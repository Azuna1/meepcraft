package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import net.minecraft.client.Minecraft;

public class ChunkProviderClient implements IChunkProvider
{
    
    private static int CHUNK_CACHE_SIZE = 800;
    private HashSet<ChunkContainer> chunkCache = new HashSet<ChunkContainer>();
    private final Minecraft mc = Minecraft.getMinecraft();

    /**
     * The completely empty chunk used by ChunkProviderClient when chunkMapping doesn't contain the requested
     * coordinates.
     */
    private Chunk blankChunk;

    /**
     * The mapping between ChunkCoordinates and Chunks that ChunkProviderClient maintains.
     */
    private LongHashMap chunkMapping = new LongHashMap();

    /**
     * This may have been intended to be an iterable version of all currently loaded chunks (MultiplayerChunkCache),
     * with identical contents to chunkMapping's values. However it is never actually added to.
     */
    private List chunkListing = new ArrayList();

    /** Reference to the World object. */
    private World worldObj;

    public ChunkProviderClient(World par1World)
    {
        this.blankChunk = new EmptyChunk(par1World, 0, 0);
        this.worldObj = par1World;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    public boolean chunkExists(int par1, int par2)
    {
        return true;
    }

    /**
     * Unload chunk from ChunkProviderClient's hashmap. Called in response to a Packet50PreChunk with its mode field set
     * to false
     */
    public void unloadChunk(int par1, int par2)
    {
        ChunkContainer chunkC = new ChunkContainer(this.provideChunk(par1, par2));
        chunkCache.add(chunkC);
        
        /* If our cache is full, replace the chunk farthest
           away from the current player position with this chunk
           and unload the old one */
        ChunkContainer farthestChunk = null;

        if(chunkCache.size() > CHUNK_CACHE_SIZE)
        {
            double maxDistance = 0;
            double playerX = mc.thePlayer.posX / 16;
            double playerZ = mc.thePlayer.posZ / 16;
            for(ChunkContainer c : chunkCache)
            {
                /* Well, not really the distance, but since sqrt() is strictly monotonic we can omit it */
                double distance = (Math.pow(c.getX() - playerX,2) + Math.pow(c.getZ() - playerZ,2));
                if( distance > maxDistance )
                {
                    maxDistance = distance;
                    farthestChunk = c;
                }
             }
         }
        
        /* Call onChunkUnload even if we don't really unload the chunk yet.
         * This marks entities on this chunk for removal (and might be a hook for other plugins) */
        if (!chunkC.c.isEmpty())
        {
            chunkC.c.onChunkUnload();
        }
        
        if (null != farthestChunk)
        {
            this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(farthestChunk.getX(), farthestChunk.getZ()));
            this.chunkListing.remove(farthestChunk.c); 
            chunkCache.remove(farthestChunk);
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public Chunk loadChunk(int par1, int par2)
    {
        Chunk var3 = new Chunk(this.worldObj, par1, par2);
        this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(par1, par2), var3);
        var3.isChunkLoaded = true;
        return var3;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int par1, int par2)
    {
        Chunk var3 = (Chunk)this.chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(par1, par2));
        return var3 == null ? this.blankChunk : var3;
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        return true;
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks()
    {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return false;
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3) {}

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "MultiplayerChunkCache: " + this.chunkMapping.getNumHashElements();
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        return null;
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int par4, int par5)
    {
        return null;
    }

    public int getLoadedChunkCount()
    {
        return this.chunkListing.size();
    }

    public void recreateStructures(int par1, int par2) {}
}
