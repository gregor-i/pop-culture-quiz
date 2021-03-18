/*
 * This is a copy of 'de.sciss.jump3r.Main',
 * but striped down for a single use case: converting a wav file into a mp3 file.
 *
 * The original Main wrote some stuff on System.out, even with the --silent option set.
 */

/*
 *      Command line frontend program
 *
 *      Copyright (c) 1999 Mark Taylor
 *                    2000 Takehiro TOMINAGA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */


package service;

import de.sciss.jump3r.mp3.*;
import de.sciss.jump3r.mp3.GetAudio.sound_file_format;
import de.sciss.jump3r.mpg.MPGLib;

import java.io.*;

class ConvertWavToMp3 {

    private DataOutput init_files(final LameGlobalFlags gf,
                                 final File input, final File output, final Enc enc, final GetAudio gaud) {

        /*
         * open the wav/aiff/raw pcm or mp3 input file. This call will open the
         * file, try to parse the headers and set gf.samplerate,
         * gf.num_channels, gf.num_samples. if you want to do your own file
         * input, skip this call and set samplerate, num_channels and
         * num_samples yourself.
         */
        gaud.init_infile(gf, input.getAbsolutePath(), enc);

        DataOutput outf = gaud.init_outfile(output.getAbsolutePath());
        if (outf == null) {
            System.err.printf("Can't init outfile '%s'\n", output.getAbsolutePath());
            return null;
        }

        return outf;
    }

    private int write_xing_frame(final LameGlobalFlags gf,
                                 final RandomAccessFile outf, VBRTag vbr) {
        byte[] mp3buffer = new byte[Lame.LAME_MAXMP3BUFFER];

        int imp3 = vbr.getLameTagFrame(gf, mp3buffer);
        if (imp3 > mp3buffer.length) {
            System.err
                    .printf("Error writing LAME-tag frame: buffer too small: buffer size=%d  frame size=%d\n",
                            mp3buffer.length, imp3);
            return -1;
        }
        if (imp3 <= 0) {
            return 0;
        }
        try {
            outf.write(mp3buffer, 0, imp3);
        } catch (IOException e) {
            System.err.println("Error writing LAME-tag");
            return -1;
        }
        return imp3;
    }

    private int lame_encoder(final LameGlobalFlags gf,
                             final DataOutput outf,
                             final File outPath,
                             final VBRTag vbr,
                             final GetAudio gaud,
                             final ID3Tag id3,
                             final Lame lame) {
        byte[] mp3buffer = new byte[Lame.LAME_MAXMP3BUFFER];
        int[][] Buffer = new int[2][1152];
        int iread;

        int imp3 = id3.lame_get_id3v2_tag(gf, mp3buffer, mp3buffer.length);
        if (imp3 > mp3buffer.length) {
            System.err
                    .printf("Error writing ID3v2 tag: buffer too small: buffer size=%d  ID3v2 size=%d\n",
                            mp3buffer.length, imp3);
            return 1;
        }
        try {
            outf.write(mp3buffer, 0, imp3);
        } catch (IOException e) {
            System.err.print("Error writing ID3v2 tag \n");
            return 1;
        }
        int id3v2_size = imp3;

        /* encode until we hit eof */
        do {
            /* read in 'iread' samples */
            iread = gaud.get_audio(gf, Buffer);

            if (iread >= 0) {
                /* encode */
                imp3 = lame.lame_encode_buffer_int(gf, Buffer[0], Buffer[1],
                        iread, mp3buffer, 0, mp3buffer.length);

                /* was our output buffer big enough? */
                if (imp3 < 0) {
                    if (imp3 == -1)
                        System.err.print("mp3 buffer is not big enough... \n");
                    else
                        System.err.printf(
                                "mp3 internal error:  error code=%d\n", imp3);
                    return 1;
                }

                try {
                    outf.write(mp3buffer, 0, imp3);
                } catch (IOException e) {
                    System.err.print("Error writing mp3 output \n");
                    return 1;
                }
            }
        } while (iread > 0);

        /*
         * may return one more mp3 frame
         */
        imp3 = lame.lame_encode_flush(gf, mp3buffer, 0, mp3buffer.length);


        if (imp3 < 0) {
            if (imp3 == -1)
                System.err.print("mp3 buffer is not big enough... \n");
            else
                System.err.printf("mp3 internal error:  error code=%d\n", imp3);
            return 1;

        }

        try {
            outf.write(mp3buffer, 0, imp3);
        } catch (IOException e) {
            System.err.print("Error writing mp3 output \n");
            return 1;
        }

        imp3 = id3.lame_get_id3v1_tag(gf, mp3buffer, mp3buffer.length);
        if (imp3 > mp3buffer.length) {
            System.err
                    .printf("Error writing ID3v1 tag: buffer too small: buffer size=%d  ID3v1 size=%d\n",
                            mp3buffer.length, imp3);
        } else {
            if (imp3 > 0) {
                try {
                    outf.write(mp3buffer, 0, imp3);
                } catch (IOException e) {
                    System.err.print("Error writing ID3v1 tag \n");
                    return 1;
                }
            }
        }

        try {
            ((Closeable) outf).close();
            RandomAccessFile rf = new RandomAccessFile(outPath, "rw");
            rf.seek(id3v2_size);
            write_xing_frame(gf, rf, vbr);
            rf.close();
        } catch (IOException e) {
            System.err.print("fatal error: can't update LAME-tag frame!\n");
        }

        return 0;
    }

    public int run(File inputFile, File outputFile) throws IOException {
        // encoder modules
        Lame lame = new Lame();
        GetAudio gaud = new GetAudio();
        GainAnalysis ga = new GainAnalysis();
        BitStream bs = new BitStream();
        Presets p = new Presets();
        QuantizePVT qupvt = new QuantizePVT();
        Quantize qu = new Quantize();
        VBRTag vbr = new VBRTag();
        Version ver = new Version();
        ID3Tag id3 = new ID3Tag();
        Reservoir rv = new Reservoir();
        Takehiro tak = new Takehiro();
        Parse parse = new Parse();

        MPGLib mpg = new MPGLib();

        lame.setModules(ga, bs, p, qupvt, qu, vbr, ver, id3, mpg);
        bs.setModules(ga, mpg, ver, vbr);
        id3.setModules(bs, ver);
        p.setModules(lame);
        qu.setModules(bs, rv, qupvt, tak);
        qupvt.setModules(tak, rv, lame.enc.psy);
        rv.setModules(bs);
        tak.setModules(qupvt);
        vbr.setModules(lame, bs, ver);
        gaud.setModules(parse, mpg);
        parse.setModules(ver, id3, p);

        /* add variables for encoder delay/padding */
        Enc enc = new Enc();
        DataOutput outf;

        /* initialize libmp3lame */
        parse.input_format = sound_file_format.sf_unknown;
        LameGlobalFlags gf = lame.lame_init();

        gf.internal_flags.tag_spec = new ID3TagSpec();
        gf.VBR = VbrMode.vbr_mtrh;


        /*
         * initialize input file. This also sets samplerate and as much other
         * data on the input file as available in the headers
         */
        outf = init_files(gf, inputFile, outputFile, enc, gaud);
        /*
         * turn off automatic writing of ID3 tag data into mp3 stream we have to
         * call it before 'lame_init_params', because that function would spit
         * out ID3v2 tag data.
         */
        gf.write_id3tag_automatic = false;

        /*
         * Now that all the options are set, lame needs to analyze them and set
         * some more internal options and check for problems
         */
        int i = lame.lame_init_params(gf);
        if (i < 0) {
            if (i == -1) {
                parse.display_bitrates(System.err);
            }
            System.err.println("fatal error during initialization");
            lame.lame_close(gf);
            return i;
        }

        /*
         * encode a single input file
         */

        int ret = lame_encoder(gf, outf, outputFile, vbr, gaud, id3, lame);

        ((Closeable) outf).close();
        gaud.close_infile();

        lame.lame_close(gf);
        return ret;
    }

}
