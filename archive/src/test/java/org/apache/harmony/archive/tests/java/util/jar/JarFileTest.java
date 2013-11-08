/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.archive.tests.java.util.jar;

import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import libcore.io.Streams;

public class JarFileTest extends TestCase {
    private static final String JAR1 = "hyts_patch.jar";

    private static final String JAR2 = "hyts_patch2.jar";

    private static final String JAR1_ENTRY1 = "foo/bar/A.class";

    @Override
    protected void setUp() {
    }

    /**
     * Constructs JarFile object.
     *
     * @tests java.util.jar.JarFile#JarFile(java.io.File)
     * @tests java.util.jar.JarFile#JarFile(java.lang.String)
     */
    public void testConstructor() throws IOException {
        File f = getFile(JAR1);

        assertTrue(new JarFile(f).getEntry(JAR1_ENTRY1).getName().equals(JAR1_ENTRY1));
        assertTrue(new JarFile(f.getPath()).getEntry(JAR1_ENTRY1).getName().equals(JAR1_ENTRY1));
    }

    /**
     * @tests java.util.jar.JarFile#entries()
     */
    public void testEntries() throws Exception {
        JarFile jarFile = new JarFile(getFile(JAR1));
        Enumeration<JarEntry> e = jarFile.entries();
        int i;
        for (i = 0; e.hasMoreElements(); i++) {
            e.nextElement();
        }
        assertEquals(jarFile.size(), i);
        jarFile.close();
        assertEquals(6, i);
    }

    public void testEntriesIterator() throws Exception {
        JarFile jarFile = new JarFile(getFile(JAR1));
        Enumeration<JarEntry> enumeration = jarFile.entries();
        jarFile.close();
        try {
            enumeration.hasMoreElements();
            fail("hasMoreElements() did not detect a closed jar file");
        } catch (IllegalStateException e) {
        }

        jarFile = new JarFile(getFile(JAR1));
        enumeration = jarFile.entries();
        jarFile.close();
        try {
            enumeration.nextElement();
            fail("nextElement() did not detect closed jar file");
        } catch (IllegalStateException e) {
        }
    }

    public void test_getEntryLjava_lang_String() throws IOException {
        try {
            JarFile jarFile = new JarFile(getFile(JAR1));
            assertEquals("Error in returned entry", 311, jarFile.getEntry(
                    JAR1_ENTRY1).getSize());
            jarFile.close();
        } catch (Exception e) {
            fail("Exception during test: " + e.toString());
        }

        JarFile jarFile = new JarFile(getFile(JAR1));
        Enumeration<JarEntry> enumeration = jarFile.entries();
        assertTrue(enumeration.hasMoreElements());
        while (enumeration.hasMoreElements()) {
            JarEntry je = enumeration.nextElement();
            jarFile.getEntry(je.getName());
        }

        enumeration = jarFile.entries();
        assertTrue(enumeration.hasMoreElements());
        JarEntry je = enumeration.nextElement();
        try {
            jarFile.close();
            jarFile.getEntry(je.getName());
            fail("IllegalStateException expected.");
        } catch (IllegalStateException expected) {
        }
    }

    public void test_getJarEntryLjava_lang_String() throws IOException {
        JarFile jarFile = new JarFile(getFile(JAR1));
        assertEquals("Error in returned entry", 311, jarFile.getJarEntry(
                JAR1_ENTRY1).getSize());
        jarFile.close();

        jarFile = new JarFile(getFile(JAR1));
        Enumeration<JarEntry> enumeration = jarFile.entries();
        assertTrue(enumeration.hasMoreElements());
        JarEntry je = enumeration.nextElement();
        jarFile.close();
        try {
            jarFile.getJarEntry(je.getName());
            fail("IllegalStateException expected.");
        } catch (IllegalStateException ee) {
        }
    }

    /**
     * @tests java.util.jar.JarFile#getJarEntry(java.lang.String)
     */
    public void testGetJarEntry() throws Exception {
        JarFile jarFile = new JarFile(getFile(JAR1));
        assertEquals("Error in returned entry", 311, jarFile.getEntry(
                JAR1_ENTRY1).getSize());
        jarFile.close();
    }

    /**
     * @tests java.util.jar.JarFile#getManifest()
     */
    public void testGetManifest() throws Exception {
        // Test for method java.util.jar.Manifest
        // java.util.jar.JarFile.getManifest()
        JarFile jarFile = new JarFile(getFile(JAR1));
        InputStream is = jarFile.getInputStream(jarFile.getEntry(JAR1_ENTRY1));
        assertTrue(is.available() > 0);
        assertNotNull("Error--Manifest not returned", jarFile.getManifest());
        jarFile.close();

        jarFile = new JarFile(getFile(JAR2));
        assertNull("Error--should have returned null", jarFile.getManifest());
        jarFile.close();

        // jarName3 was created using the following test
        jarFile = new JarFile(getFile("hyts_manifest1.jar"));
        assertNotNull("Should find manifest without verifying", jarFile.getManifest());
        jarFile.close();

        // this is used to create jarName3 used in the previous test
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(new Attributes.Name("Manifest-Version"), "1.0");
        ByteArrayOutputStream manOut = new ByteArrayOutputStream();
        manifest.write(manOut);
        byte[] manBytes = manOut.toByteArray();
        File file = File.createTempFile("test", ".jar");
        try {
            JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(file
                    .getAbsolutePath()));
            ZipEntry entry = new ZipEntry("META-INF/");
            entry.setSize(0);
            jarOut.putNextEntry(entry);
            entry = new ZipEntry(JarFile.MANIFEST_NAME);
            entry.setSize(manBytes.length);
            jarOut.putNextEntry(entry);
            jarOut.write(manBytes);
            entry = new ZipEntry("myfile");
            entry.setSize(1);
            jarOut.putNextEntry(entry);
            jarOut.write(65);
            jarOut.close();
            JarFile jar = new JarFile(file.getAbsolutePath(), false);
            assertNotNull("Should find manifest without verifying", jar
                    .getManifest());
            jar.close();
        } finally {
            file.delete();
        }

        try {
            JarFile jF = new JarFile(getFile(JAR2));
            jF.close();
            jF.getManifest();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) {
            // expected;
        }
    }

    /**
     * @tests java.util.jar.JarFile#getInputStream(java.util.zip.ZipEntry)
     */
    public void testGetInputStream() throws Exception {
        JarFile jf = new JarFile(getFile(JAR1));

        InputStream is = jf.getInputStream(new JarEntry("invalid"));
        assertNull("Got stream for non-existent entry", is);

        is = jf.getInputStream(jf.getEntry(JAR1_ENTRY1));
        assertTrue("Returned invalid stream", is.available() > 0);

        // try to read class file header
        byte[] b = new byte[4];
        Streams.readFully(is, b);
        jf.close();
        assertEquals("Invalid bytes were read", (byte) 0xCA, b[0]);
        assertEquals("Invalid bytes were read", (byte) 0xFE, b[1]);
        assertEquals("Invalid bytes were read", (byte) 0xBA, b[2]);
        assertEquals("Invalid bytes were read", (byte) 0xBE, b[3]);
    }

    /**
     * Signed file is verified by default.
     *
     * @tests java.util.jar.JarFile#getInputStream(java.util.zip.ZipEntry)
     */
    public void testInputStreamOperations() throws Exception {
        final String inputJar = "hyts_signed.jar";
        final String signedEntry = "coucou/FileAccess.class";

        JarFile jar = new JarFile(getFile(inputJar));
        JarEntry entry = new JarEntry(signedEntry);
        InputStream in = jar.getInputStream(entry);
        in.read();

        // RI verifies only entries which appear via getJarEntry method
        jar = new JarFile(getFile(inputJar));
        entry = jar.getJarEntry(signedEntry);
        in = jar.getInputStream(entry);
        readExactly(in, (int) entry.getSize() - 1);
        assertNull(entry.getCertificates());
        in.read();
        assertNotNull(entry.getCertificates());
        assertEquals(-1, in.read());

        jar = new JarFile(getFile(inputJar));
        entry = jar.getJarEntry(signedEntry);
        entry.setSize(entry.getSize() - 1);
        in = jar.getInputStream(entry);
        readExactly(in, (int) entry.getSize() - 1);
        assertNull(entry.getCertificates());
        try {
            in.read();
            fail("SecurityException expected");
        } catch (SecurityException e) {
            // desired
        }
        assertEquals(-1, in.read());
    }

    /**
     * Performs as many read() calls as necessary to read {@code numBytes} from
     * the stream. Should the stream exhaust early, this method will fail.
     */
    private static void readExactly(InputStream in, int numBytes) throws IOException {
        byte[] buffer = new byte[1024];
        while (numBytes > 0) {
            int read = in.read(buffer, 0, Math.min(numBytes, 1024));
            assertTrue(read != -1);
            numBytes -= read;
        }
    }

    /*
     * The jar created by 1.4 which does not provide a
     * algorithm-Digest-Manifest-Main-Attributes entry in .SF file.
     */
    public void testJar14() throws IOException {
        JarFile jarFile = new JarFile(getFile("Created_by_1_4.jar"), true);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            jarFile.getInputStream(zipEntry).skip(Long.MAX_VALUE);
        }
    }

    /**
     * The jar is intact, then everything is all right.
     */
    public void testJarVerification() throws IOException {
        JarFile jarFile = new JarFile(getFile("Integrate.jar"), true);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            jarFile.getInputStream(zipEntry).skip(Long.MAX_VALUE);
        }
    }

    /**
     * The jar is intact, but the entry object is modified.
     */
    public void testJarVerificationModifiedEntry() throws IOException {
        final String inputJar = "Integrate.jar";
        final String signedEntry = "Test.class";

        JarFile jarFile = new JarFile(getFile(inputJar));
        ZipEntry zipEntry = jarFile.getJarEntry(signedEntry);
        zipEntry.setSize(zipEntry.getSize() + 1);
        jarFile.getInputStream(zipEntry).skip(Long.MAX_VALUE);

        jarFile = new JarFile(getFile(inputJar));
        zipEntry = jarFile.getJarEntry(signedEntry);
        zipEntry.setSize(zipEntry.getSize() - 1);
        try {
            //jarFile.getInputStream(zipEntry).skip(Long.MAX_VALUE);
            jarFile.getInputStream(zipEntry).read(new byte[5000], 0, 5000);
            fail("SecurityException expected");
        } catch (SecurityException e) {
            // desired
        }
    }

    /*
     * If another entry is inserted into Manifest, no security exception will be
     * thrown out.
     */
    public void testJarFileInsertEntryInManifestJar() throws IOException {
        JarFile jarFile = new JarFile(getFile("Inserted_Entry_Manifest.jar"), true);
        Enumeration<JarEntry> entries = jarFile.entries();
        int count = 0;
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            jarFile.getInputStream(zipEntry);
            count++;
        }
        assertEquals(5, count);
    }

    /*
     * If another entry is inserted into Manifest, no security exception will be
     * thrown out.
     */
    public void testInsertedEntryManifestWithDigestCode() throws IOException {
        JarFile jarFile = new JarFile(getFile("Inserted_Entry_Manifest_with_DigestCode.jar"), true);
        Enumeration<JarEntry> entries = jarFile.entries();
        int count = 0;
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            jarFile.getInputStream(zipEntry);
            count++;
        }
        assertEquals(5, count);
    }

    /*
     * The content of Test.class is modified, jarFile.getInputStream will not
     * throw security Exception, but it will anytime before the inputStream got
     * from getInputStream method has been read to end.
     */
    public void testJarFileModifiedClass() throws IOException {
        JarFile jarFile = new JarFile(getFile("Modified_Class.jar"), true);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            jarFile.getInputStream(zipEntry);
        }
        /* The content of Test.class has been tampered. */
        ZipEntry zipEntry = jarFile.getEntry("Test.class");
        InputStream in = jarFile.getInputStream(zipEntry);
        byte[] buffer = new byte[1024];
        try {
            while (in.available() > 0) {
                in.read(buffer);
            }
            fail("SecurityException expected");
        } catch (SecurityException e) {
            // desired
        }
    }

    /*
     * In the Modified.jar, the main attributes of META-INF/MANIFEST.MF is
     * tampered manually. Hence the RI 5.0 JarFile.getInputStream of any
     * JarEntry will throw security exception.
     */
    public void testJarFileModifiedManifestMainAttributes() throws IOException {
        JarFile jarFile = new JarFile(getFile("Modified_Manifest_MainAttributes.jar"), true);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            try {
                jarFile.getInputStream(zipEntry);
                fail("SecurityException expected");
            } catch (SecurityException e) {
                // desired
            }
        }
    }

    /*
     * It is all right in our original JarFile. If the Entry Attributes, for
     * example Test.class in our jar, the jarFile.getInputStream will throw
     * Security Exception.
     */
    public void testJarFileModifiedManifestEntryAttributes() throws IOException {
        JarFile jarFile = new JarFile(getFile("Modified_Manifest_EntryAttributes.jar"),
                true);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            try {
                jarFile.getInputStream(zipEntry);
                fail("should throw Security Exception");
            } catch (SecurityException e) {
                // desired
            }
        }
    }

    /*
     * If the content of the .SA file is modified, no matter what it resides,
     * JarFile.getInputStream of any JarEntry will throw Security Exception.
     */
    public void testJarFileModifiedSfEntryAttributes() throws IOException {
        JarFile jarFile = new JarFile(getFile("Modified_SF_EntryAttributes.jar"), true);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            try {
                jarFile.getInputStream(zipEntry);
                fail("should throw Security Exception");
            } catch (SecurityException e) {
                // desired
            }
        }
    }

    public void testGetInputStreamLjava_util_jar_JarEntry() throws IOException {
        JarFile jf = new JarFile(getFile(JAR1));
        InputStream is = jf.getInputStream(jf.getEntry(JAR1_ENTRY1));
        assertTrue(is.available() > 0);

        byte[] buffer = new byte[1024];
        int r = is.read(buffer, 0, 1024);
        jf.close();
        is.close();

        StringBuilder sb = new StringBuilder(r);
        for (int i = 0; i < r; i++) {
            sb.append((char) (buffer[i] & 0xff));
        }
        String contents = sb.toString();
        assertTrue(contents.indexOf("foo") > 0);
        assertTrue(contents.indexOf("bar") > 0);

        try {
            jf.getInputStream(jf.getEntry(JAR1_ENTRY1));
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }

        jf = new JarFile(getFile(JAR1));
        is = jf.getInputStream(new JarEntry("invalid"));
        assertNull(is);
        jf.close();
    }

    public void testJarVerificationEmptyEntry() throws IOException {
        JarFile jarFile = new JarFile(getFile("EmptyEntries_signed.jar"));

        ZipEntry zipEntry = jarFile.getJarEntry("subfolder/internalSubset01.js");
        int res = jarFile.getInputStream(zipEntry).read(new byte[100], 0, 100);
        assertEquals("Wrong length of empty jar entry", -1, res);

        zipEntry = jarFile.getJarEntry("svgtest.js");
        res = jarFile.getInputStream(zipEntry).read(new byte[100], 0, 100);
        assertEquals("Wrong length of empty jar entry", -1, res);

        zipEntry = jarFile.getJarEntry("svgunit.js");
        res = jarFile.getInputStream(zipEntry).read();
        assertEquals("Wrong length of empty jar entry", -1, res);
    }

    public static File getFile(String fileName) {
        return new File("/sdcard/test_resources/" + fileName);
    }
}
