/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (c) 2005 Christian Schulte <cs@schulte.it>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.jdtaus.banking.dtaus.ri.zka;

import java.io.IOException;
import java.util.Locale;
import org.jdtaus.banking.dtaus.Checksum;
import org.jdtaus.banking.dtaus.Header;
import org.jdtaus.banking.dtaus.LogicalFile;
import org.jdtaus.banking.dtaus.PhysicalFile;
import org.jdtaus.banking.dtaus.ri.zka.messages.AnalyzingFileMessage;
import org.jdtaus.banking.dtaus.spi.IllegalHeaderException;
import org.jdtaus.core.container.ContainerFactory;
import org.jdtaus.core.container.ContextFactory;
import org.jdtaus.core.container.ContextInitializer;
import org.jdtaus.core.container.Implementation;
import org.jdtaus.core.container.ModelFactory;
import org.jdtaus.core.container.Properties;
import org.jdtaus.core.container.Property;
import org.jdtaus.core.io.StructuredFileListener;
import org.jdtaus.core.io.util.StructuredFileOperations;
import org.jdtaus.core.monitor.spi.Task;
import org.jdtaus.core.monitor.spi.TaskMonitor;
import org.jdtaus.core.text.Message;

/**
 * Default {@code PhysicalFile}-Implementierung.
 * <p/>
 * <b>Hinweis:</b><br/>
 * Implementierung darf niemals von mehreren Threads gleichzeitig verwendet
 * werden.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $Id$
 */
public final class DefaultPhysicalFile implements PhysicalFile
{

    //--Attribute---------------------------------------------------------------

    /** Index der logischen Dateien. */
    protected AbstractLogicalFile[] index = new AbstractLogicalFile[100];

    /** Anzahl vorhandener logischer Dateien. */
    private int dtausCount = 0;

    //---------------------------------------------------------------Attribute--
    //--Implementation----------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Meta-data describing the implementation. */
    private static final Implementation META =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultPhysicalFile.class.getName());

    //----------------------------------------------------------Implementation--
    //--Constructors------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /**
     * Initializes the properties of the instance.
     *
     * @param meta the property values to initialize the instance with.
     *
     * @throws NullPointerException if {@code meta} is {@code null}.
     */
    protected void initializeProperties(final Properties meta)
    {
        Property p;

        if(meta == null)
        {
            throw new NullPointerException("meta");
        }

    }

    //------------------------------------------------------------Constructors--
    //--Konstruktoren-----------------------------------------------------------

    /**
     * Creates a new {@code DefaultPhysicalFile} instance.
     *
     * @param structuredFile the {@code StructuredFile} implementation to
     * operate on.
     *
     * @throws NullPointerException {@code if(structuredFile == null)}
     * @throws IOException wenn nicht gelesen werden kann.
     */
    public DefaultPhysicalFile(
        final StructuredFileOperations structuredFile) throws IOException
    {

        super();
        this.initializeProperties(DefaultPhysicalFile.META.getProperties());

        if(structuredFile == null)
        {
            throw new NullPointerException("structuredFile");
        }

        this.structuredFile = structuredFile;
        this.structuredFile.addStructuredFileListener(
            new StructuredFileListener()
        {

            public void blocksInserted(long l, long l0) throws IOException
            {
                final int fileIndex;

                if((fileIndex = this.getFileIndex(l)) >= 0)
                {
                    // Increment properties headerBlock and checksumBlock for
                    // all remaining files.
                    for(int i = fileIndex + 1; i < dtausCount; i++)
                    {
                        index[i].setHeaderBlock(index[i].getHeaderBlock() + l0);
                        index[i].setChecksumBlock(
                            index[i].getChecksumBlock() + l0);

                    }
                }
            }

            public void blocksDeleted(long l, long l0) throws IOException
            {
                final int fileIndex;

                if((fileIndex = this.getFileIndex(l)) >= 0)
                {
                    // Decrement properties headerBlock and checksumBlock for
                    // all remaining files.
                    for(int i = fileIndex + 1; i < dtausCount; i++)
                    {
                        index[i].setHeaderBlock(index[i].getHeaderBlock() - l0);
                        index[i].setChecksumBlock(
                            index[i].getChecksumBlock() - l0);

                    }
                }
            }

            private int getFileIndex(final long block)
            {
                int i;
                for(i = dtausCount - 1; i >= 0; i--)
                {
                    if(block >= index[i].getHeaderBlock() &&
                        block <= index[i].getChecksumBlock())
                    {
                        break;
                    }
                }

                return i;
            }
        });
    }

    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

    // This section is managed by jdtaus-container-mojo.

    /** Configured <code>TaskMonitor</code> implementation. */
    private transient TaskMonitor _dependency0;

    /**
     * Gets the configured <code>TaskMonitor</code> implementation.
     *
     * @return the configured <code>TaskMonitor</code> implementation.
     */
    private TaskMonitor getTaskMonitor()
    {
        TaskMonitor ret = null;
        if(this._dependency0 != null)
        {
            ret = this._dependency0;
        }
        else
        {
            ret = (TaskMonitor) ContainerFactory.getContainer().
                getDependency(DefaultPhysicalFile.class,
                "TaskMonitor");

            if(ModelFactory.getModel().getModules().
                getImplementation(DefaultPhysicalFile.class.getName()).
                getDependencies().getDependency("TaskMonitor").
                isBound())
            {
                this._dependency0 = ret;
            }
        }

        if(ret instanceof ContextInitializer && !((ContextInitializer) ret).
            isInitialized(ContextFactory.getContext()))
        {
            ((ContextInitializer) ret).initialize(ContextFactory.getContext());
        }

        return ret;
    }

    //------------------------------------------------------------Dependencies--
    //--PhysicalFile------------------------------------------------------------

    protected void checksum() throws IOException
    {
        this.dtausCount = 0;
        int dtausIndex = 0;
        final long blockCount = this.getStructuredFile().getBlockCount();
        final int maximumProgress = blockCount > Integer.MAX_VALUE ?
            Integer.MAX_VALUE : (int) blockCount;

        final Task task = new Task();
        task.setIndeterminate(false);
        task.setCancelable(false);
        task.setDescription(new AnalyzingFileMessage());
        task.setMinimum(0);
        task.setProgress(0);
        task.setMaximum(maximumProgress);

        try
        {
            this.getTaskMonitor().monitor(task);

            for(long block = 0L; block < blockCount;)
            {
                task.setProgress(block > maximumProgress ?
                    maximumProgress : (int) block);

                this.resizeIndex(dtausIndex);
                this.index[dtausIndex] = this.newLogicalFile(block);
                this.index[dtausIndex].checksum();
                block = this.index[dtausIndex++].getChecksumBlock() + 1L;
                this.dtausCount++;
            }
        }
        finally
        {
            this.getTaskMonitor().finish(task);
        }
    }

    public int count()
    {
        return this.dtausCount;
    }

    protected boolean checkLogicalFileExists(int dtausId)
    {
        return dtausId < this.dtausCount && dtausId >= 0;
    }

    public LogicalFile add(final Header header) throws IOException
    {
        if(header == null)
        {
            throw new NullPointerException("header");
        }
        final Checksum newChecksum = new Checksum();
        final AbstractLogicalFile lFile = this.newLogicalFile(
            this.dtausCount == 0 ? 0L :
                this.index[this.dtausCount - 1].getChecksumBlock() + 1L);

        final IllegalHeaderException e = lFile.checkHeader(header);
        if(e != null && e.getMessages().length > 0)
        {
            throw e;
        }

        this.getStructuredFile().insertBlocks(this.dtausCount == 0 ?
            0L : this.index[this.dtausCount - 1].getChecksumBlock() + 1L, 2L);

        this.resizeIndex(this.dtausCount);
        this.index[this.dtausCount] = lFile;
        lFile.writeHeader(this.index[this.dtausCount].getHeaderBlock(),
            header);

        lFile.writeChecksum(this.index[this.dtausCount].
            getHeaderBlock() + 1L, newChecksum);

        this.getStructuredFile().flush();
        this.index[this.dtausCount].checksum();
        return this.index[this.dtausCount++];
    }

    public LogicalFile get(int dtausId)
    {
        if(!this.checkLogicalFileExists(dtausId))
        {
            throw new IllegalArgumentException("dtausId");
        }
        return this.index[dtausId];
    }

    public void remove(int dtausId) throws IOException
    {
        if(!this.checkLogicalFileExists(dtausId))
        {
            throw new IllegalArgumentException("dtausId");
        }
        this.getStructuredFile().deleteBlocks(
            this.index[dtausId].getHeaderBlock(),
            this.index[dtausId].getChecksumBlock() -
            this.index[dtausId].getHeaderBlock() + 1);

        System.arraycopy(this.index, dtausId + 1, this.index, dtausId,
            --this.dtausCount - dtausId);

        this.getStructuredFile().flush();
    }

    //------------------------------------------------------------PhysicalFile--
    //--DefaultPhysicalFile-----------------------------------------------------

    /** <code>StructuredFile</code> requirement. **/
    private StructuredFileOperations structuredFile;

    /** StructuredFileOperations requirement getter method. */
    protected StructuredFileOperations getStructuredFile()
    {
        return this.structuredFile;
    }

    protected AbstractLogicalFile newLogicalFile(
        final long headerBlock) throws IOException
    {

        final AbstractLogicalFile ret;

        switch(this.getStructuredFile().getBlockSize())
        {
            case 128:
                ret = new DTAUSDisk(headerBlock, this.getStructuredFile());
                break;
            case 150:
                ret = new DTAUSTape(headerBlock, this.getStructuredFile());
                break;
            default:
                throw new IllegalStateException();

        }

        return ret;
    }

    protected void resizeIndex(int requested)
    {
        if(this.index.length - 1 < requested)
        {
            while(this.index.length - 1 <= requested)
            {
                final int newLength = this.index.length * 2;
                final AbstractLogicalFile[] newIndex =
                    new AbstractLogicalFile[newLength];

                System.arraycopy(this.index, 0, newIndex, 0, this.index.length);
                this.index = newIndex;
            }
        }
    }

    //-----------------------------------------------------DefaultPhysicalFile--

}
