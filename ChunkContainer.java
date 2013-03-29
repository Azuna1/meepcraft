package net.minecraft.src;


/**
  * Container class for Chunk because Chunk does not implement equals/hashCode
  * We dont want to modify Chunk itself, thus this Container wraps it.
  */
public class ChunkContainer
{
    public Chunk c;

    /* The position of the Chunk is immutable, we can therefore
     * cache the hashCode */
    private final int hashCode;

    public ChunkContainer(Chunk c)
    {
        this.c = c;

        /* Calculate the hashCode for this Chunk
         * See ch. 3 of "Effective Java"
         * for reasoning behind this hash funktion */
        int result = 17;
        result = 37 * result + getX();
        result = 37 * result + getZ();
        hashCode = result;
    }

    @Override
    public boolean equals(Object o)
    {
      if ( o instanceof ChunkContainer && c.xPosition == ((ChunkContainer)o).c.xPosition && c.zPosition == ((ChunkContainer)o).c.zPosition )
        return true;
      return false;
    }

    @Override
    public int hashCode() {
         return hashCode;
    }

    public int getX() { return c.xPosition; }
    public int getZ() { return c.zPosition; }

}
