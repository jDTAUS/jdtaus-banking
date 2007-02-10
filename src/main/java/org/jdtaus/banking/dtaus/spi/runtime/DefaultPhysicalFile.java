/*
 *  jDTAUS - DTAUS fileformat.
 *  Copyright (C) 2005 Christian Schulte <cs@schulte.it>
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
package org.jdtaus.common.dtaus.impl;

import org.jdtaus.common.container.model.Dependency;
import org.jdtaus.common.container.model.Implementation;
import org.jdtaus.common.container.model.ModelFactory;
import org.jdtaus.common.dtaus.Checksum;
import org.jdtaus.common.dtaus.Header;
import org.jdtaus.common.dtaus.LogicalFile;
import org.jdtaus.common.dtaus.PhysicalFile;
import org.jdtaus.common.dtaus.PhysicalFileError;
import org.jdtaus.common.io.StructuredFileListener;
import org.jdtaus.common.io.StructuredFileOperations;

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
public class DefaultPhysicalFile implements PhysicalFile {
    
    //--Implementation----------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** Metadaten dieser Implementierung. */
    private static final Implementation IMPL =
        ModelFactory.getModel().getModules().
        getImplementation(DefaultPhysicalFile.class.getName());

    //----------------------------------------------------------Implementation--
    //--Attribute---------------------------------------------------------------
    
    /** Index der logischen Dateien. */
    protected AbstractLogicalFile[] index = new AbstractLogicalFile[100];
    
    /** Anzahl vorhandener logischer Dateien. */
    private int dtausCount = 0;
    
    //---------------------------------------------------------------Attribute--
    //--Constructors------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.

    /** <code>DefaultPhysicalFile</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Implementierung.
    */ 
    protected DefaultPhysicalFile(final Implementation meta) {
        super();
    }
    /** <code>DefaultPhysicalFile</code> Implementierungs-Konstruktor.
    * @param meta Metadaten der Dependency.
    */ 
    protected DefaultPhysicalFile(final Dependency meta) {
        super();
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
     */
    protected DefaultPhysicalFile(
        final StructuredFileOperations structuredFile) {

        this(DefaultPhysicalFile.IMPL);
        
        if(structuredFile == null) {
            throw new NullPointerException("structuredFile");
        }
        
        this.structuredFile = structuredFile;
        this.structuredFile.addStructuredFileListener(
            new StructuredFileListener() {
            
            public void blocksInserted(long l, long l0) {
                final int fileIndex;
                
                if((fileIndex = this.getFileIndex(l)) >= 0) {
                    // Increment properties headerBlock and checksumBlock for
                    // all remaining files.
                    for(int i = fileIndex + 1; i < dtausCount; i++) {
                        index[i].setHeaderBlock(index[i].getHeaderBlock() + l0);
                        index[i].setChecksumBlock(
                            index[i].getChecksumBlock() + l0);
                        
                    }
                }
            }
            
            public void blocksDeleted(long l, long l0) {
                final int fileIndex;
                
                if((fileIndex = this.getFileIndex(l)) >= 0) {
                    // Decrement properties headerBlock and checksumBlock for
                    // all remaining files.
                    for(int i = fileIndex + 1; i < dtausCount; i++) {
                        index[i].setHeaderBlock(index[i].getHeaderBlock() - l0);
                        index[i].setChecksumBlock(
                            index[i].getChecksumBlock() - l0);

                    }
                }
            }
            
            private int getFileIndex(final long block) {
                int i;
                for(i = dtausCount - 1; i >= 0; i--) {
                    if(block >= index[i].getHeaderBlock() &&
                        block <= index[i].getChecksumBlock()) {
                        
                        break;
                    }
                }
                
                return i;
            }
        });
    }
    
    //-----------------------------------------------------------Konstruktoren--
    //--Dependencies------------------------------------------------------------

    // Dieser Abschnitt wird von jdtaus-source-plugin generiert.


    //------------------------------------------------------------Dependencies--
    //--PhysicalFile------------------------------------------------------------
    
    protected void checksum() throws PhysicalFileError {
        this.dtausCount = 0;
        int dtausIndex = 0;
        final long blockCount = this.getStructuredFile().getBlockCount();
        
        for(long block = 0L; block < blockCount;) {
            this.resizeIndex(dtausIndex);
            this.index[dtausIndex] = this.newLogicalFile(block);
            this.index[dtausIndex].checksum();
            block = this.index[dtausIndex++].getChecksumBlock() + 1L;
            this.dtausCount++;
        }
    }
    
    public int getLogicalFileCount() {
        return this.dtausCount;
    }
    
    protected boolean checkLogicalFileExists(int dtausId) {
        return dtausId < this.dtausCount && dtausId >= 0;
    }
    
    public LogicalFile createLogicalFile(
        final Header header) throws PhysicalFileError {
        
        if(header == null) {
            throw new NullPointerException("header");
        }
        final Checksum newChecksum = new Checksum();
        final AbstractLogicalFile lFile = this.newLogicalFile(
            this.dtausCount == 0 ? 0L :
                this.index[this.dtausCount - 1].getChecksumBlock() + 1L);
        
        lFile.checkHeader(header);
        
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
    
    public LogicalFile editLogicalFile(int dtausId) throws PhysicalFileError {
        if(!this.checkLogicalFileExists(dtausId)) {
            throw new IllegalArgumentException("dtausId");
        }
        return this.index[dtausId];
    }
    
    public void deleteLogicalFile(int dtausId) throws PhysicalFileError {
        if(!this.checkLogicalFileExists(dtausId)) {
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
    protected StructuredFileOperations getStructuredFile() {
        return this.structuredFile;
    }
    
    protected AbstractLogicalFile newLogicalFile(final long headerBlock) {
        final AbstractLogicalFile ret;
        
        switch(this.getStructuredFile().getBlockSize()) {
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
    
    protected void resizeIndex(int requested) {
        if(this.index.length - 1 < requested) {
            while(this.index.length - 1 <= requested) {
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
