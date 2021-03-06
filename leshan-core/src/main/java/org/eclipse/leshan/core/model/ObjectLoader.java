/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.leshan.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectLoader.class);

    static final String[] ddfpaths = new String[] { "0-1_0.xml", "1-1_0.xml", "2-1_0.xml", "3-1_0.xml", "4-1_0.xml",
            "5-1_0.xml", "6.xml", "7.xml" };

    static final String[] lastestddfspath = new String[] { "0-1_0.xml", "1-1_0.xml", "2.xml", "3-1_0.xml", "4-1_1.xml",
            "5-1_0.xml", "6.xml", "7.xml" };

    static final String[] allddfspath = new String[] { "0-1_0.xml", "1-1_0.xml", "2-1_0.xml", "2.xml", "3-1_0.xml",
            "4-1_0.xml", "4-1_1.xml", "5-1_0.xml", "6.xml", "7.xml" };

    /**
     * Load the default LWM2M objects in the version 1.0.
     * <p>
     * So there is only 1 version by object.
     */
    public static List<ObjectModel> loadDefault() {
        List<ObjectModel> models = new ArrayList<>();

        // standard objects
        LOG.debug("Loading OMA standard object models");
        models.addAll(loadDdfResources("/models/", ddfpaths));

        return models;
    }

    /**
     * Load the core LWM2M objects in their last version.
     * <p>
     * So there is only 1 version by object.
     * <p>
     * Warning : between 2 minor version of Leshan, lastest version could change for a given object.
     * 
     * @since 1.4
     */
    public static List<ObjectModel> loadLastestDefault() {
        List<ObjectModel> models = new ArrayList<>();

        // lastest standard objects
        LOG.debug("Loading OMA standard object models");
        models.addAll(loadDdfResources("/models/", lastestddfspath));

        return models;
    }

    /**
     * Load all available version of default LWM2M objects.
     * <p>
     * So you could get several versions for same object.
     * 
     * @since 1.4
     */
    public static List<ObjectModel> loadAllDefault() {
        List<ObjectModel> models = new ArrayList<>();

        // standard objects
        LOG.debug("Loading OMA standard object models");
        models.addAll(loadDdfResources("/models/", allddfspath));

        return models;
    }

    /**
     * Load object definitions from DDF or JSON files.
     * 
     * @param modelDir the directory containing the object definition files.
     * 
     * @deprecated use {@link #loadObjectsFromDir(File)} instead
     */
    @Deprecated
    public static List<ObjectModel> load(File modelDir) {
        return loadObjectsFromDir(modelDir);
    }

    /**
     * Load object definition from DDF file.
     * <p>
     * Models are not validate if you want to ensure that model are valid use
     * {@link #loadDdfFile(InputStream, String, boolean)} or
     * {@link #loadDdfFile(InputStream, String, DDFFileValidator, ObjectModelValidator)}
     * 
     * @param input An inputStream to a DDF file.
     * @param streamName A name for the stream used for logging only
     */
    public static List<ObjectModel> loadDdfFile(InputStream input, String streamName) {
        try {
            DDFFileParser ddfFileParser = new DDFFileParser();
            return ddfFileParser.parseEx(input, streamName);
        } catch (InvalidDDFFileException | IOException e) {
            // TODO change for 2.0 : we should raise an IOException
            LOG.warn("Unable to parse ddf file {}", streamName, e);
        }
        return Collections.emptyList();
    }

    /**
     * Load object definition from DDF file.
     * 
     * @param input An inputStream to a DDF file.
     * @param streamName A name for the stream used for logging only
     * @param validate true if you want model validation. Validation is not free and it could make sense to not validate
     *        model if you already trust it.
     * 
     * @throws InvalidDDFFileException if DDF file is invalid
     * @throws InvalidModelException if model is invalid
     * 
     * @since 1.1
     */
    public static List<ObjectModel> loadDdfFile(InputStream input, String streamName, boolean validate)
            throws InvalidModelException, InvalidDDFFileException, IOException {
        return loadDdfFile(input, streamName, validate ? new DefaultDDFFileValidator() : null,
                validate ? new DefaultObjectModelValidator() : null);
    }

    /**
     * Load object definition from DDF file.
     * 
     * @param input An inputStream to a DDF file.
     * @param streamName A name for the stream used for logging and validation
     * @param ddfValidator a validator used to validate DDF files, see {@link DefaultDDFFileValidator}. If {@code null}
     *        then there will be no validation.
     * @param modelValidator an Object model validator to ensure model is valid, see
     *        {@link DefaultObjectModelValidator}. If {@code null} then there will be no validation.
     * 
     * @throws InvalidDDFFileException if DDF file is invalid
     * @throws InvalidModelException if model is invalid
     * 
     * @since 1.1
     */
    public static List<ObjectModel> loadDdfFile(InputStream input, String streamName, DDFFileValidator ddfValidator,
            ObjectModelValidator modelValidator) throws InvalidModelException, InvalidDDFFileException, IOException {
        DDFFileParser ddfFileParser = new DDFFileParser(ddfValidator);
        List<ObjectModel> models = ddfFileParser.parseEx(input, streamName);
        if (modelValidator != null) {
            modelValidator.validate(models, streamName);
        }
        return models;
    }

    /**
     * Load object definition from DDF resources following rules of {@link Class#getResourceAsStream(String)}.
     * <p>
     * It should be used to load DDF embedded with your application bundle (e.g. jar, war, ...)
     * <p>
     * Models are not validate if you want to ensure that model are valid use
     * {@link #loadDdfFile(InputStream, String, boolean)} or
     * {@link #loadDdfFile(InputStream, String, DDFFileValidator, ObjectModelValidator)}
     * 
     * @param path directory path to the DDF files
     * @param filenames names of all the DDF files
     */
    public static List<ObjectModel> loadDdfResources(String path, String[] filenames) {
        try {
            return loadDdfResources(path, filenames, false);
        } catch (IOException | InvalidModelException | InvalidDDFFileException e) {
            throw new IllegalStateException("Unable to load model", e);
        }

    }

    /**
     * Load object definition from DDF resources following rules of {@link Class#getResourceAsStream(String)}.
     * <p>
     * It should be used to load DDF embedded with your application bundle (e.g. jar, war, ...)
     * 
     * @param path directory path to the DDF files
     * @param filenames names of all the DDF files
     * @param validate true if you want model validation. Validation is not free and it could make sense to not validate
     *        model if you already trust it.
     * 
     * @throws InvalidDDFFileException if DDF file is invalid
     * @throws InvalidModelException if model is invalid
     * 
     * @since 1.1
     */
    public static List<ObjectModel> loadDdfResources(String path, String[] filenames, boolean validate)
            throws IOException, InvalidModelException, InvalidDDFFileException {
        return loadDdfResources(path, filenames, validate ? new DefaultDDFFileValidator() : null,
                validate ? new DefaultObjectModelValidator() : null);
    }

    /**
     * Load object definition from DDF resources following rules of {@link Class#getResourceAsStream(String)}.
     * <p>
     * It should be used to load DDF embedded with your application bundle (e.g. jar, war, ...)
     * <p>
     * 
     * @param path directory path to the DDF files
     * @param filenames names of all the DDF files
     * @param ddfValidator a validator used to validate DDF files, see {@link DefaultDDFFileValidator}. If {@code null}
     *        then there will be no validation.
     * @param modelValidator an Object model validator to ensure model is valid, see
     *        {@link DefaultObjectModelValidator}. If {@code null} then there will be no validation.
     * 
     * @throws InvalidDDFFileException if DDF file is invalid
     * @throws InvalidModelException if model is invalid
     * 
     * @since 1.1
     */
    public static List<ObjectModel> loadDdfResources(String path, String[] filenames, DDFFileValidator ddfValidator,
            ObjectModelValidator modelValidator) throws IOException, InvalidModelException, InvalidDDFFileException {
        List<ObjectModel> models = new ArrayList<>();
        for (String filename : filenames) {
            String fullpath = StringUtils.removeEnd(path, "/") + "/" + StringUtils.removeStart(filename, "/");
            InputStream input = ObjectLoader.class.getResourceAsStream(fullpath);
            if (input != null) {
                try (Reader reader = new InputStreamReader(input)) {
                    models.addAll(loadDdfFile(input, fullpath, ddfValidator, modelValidator));
                }
            } else {
                throw new FileNotFoundException(String.format("%s not found", fullpath));
            }

        }
        return models;
    }

    /**
     * Load object definition from DDF resources following rules of {@link Class#getResourceAsStream(String)}.
     * <p>
     * It should be used to load DDF embedded with your application bundle (e.g. jar, war, ...)
     * <p>
     * Models are not validate if you want to ensure that model are valid use
     * {@link #loadDdfFile(InputStream, String, boolean)} or
     * {@link #loadDdfFile(InputStream, String, DDFFileValidator, ObjectModelValidator)}
     * 
     * @param paths An array of paths to DDF files.
     */
    public static List<ObjectModel> loadDdfResources(String[] paths) {
        List<ObjectModel> models = new ArrayList<>();
        for (String path : paths) {
            InputStream input = ObjectLoader.class.getResourceAsStream(path);
            if (input != null) {
                try (Reader reader = new InputStreamReader(input)) {
                    models.addAll(loadDdfFile(input, path));
                } catch (IOException e) {
                    throw new IllegalStateException(String.format("Unable to load model %s", path), e);
                }
            } else {
                throw new IllegalStateException(String.format("Unable to load model %s", path));
            }
        }
        return models;
    }

    /**
     * Load object definitions from directory.
     * <p>
     * Models are not validate if you want to ensure that model are valid use
     * {@link #loadDdfFile(InputStream, String, boolean)} or
     * {@link #loadDdfFile(InputStream, String, DDFFileValidator, ObjectModelValidator)}
     * 
     * @param modelsDir the directory containing all the ddf file definition.
     */
    public static List<ObjectModel> loadObjectsFromDir(File modelsDir) {
        return loadObjectsFromDir(modelsDir, null, null);
    }

    /**
     * Load object definitions from directory.
     * 
     * Invalid model will be logged and ignored.
     * 
     * @param modelsDir the directory containing all the ddf file definition.
     * @param validate true if you want model validation. Validation is not free and it could make sense to not validate
     *        model if you already trust it.
     */
    public static List<ObjectModel> loadObjectsFromDir(File modelsDir, boolean validate) {
        return loadObjectsFromDir(modelsDir, validate ? new DefaultDDFFileValidator() : null,
                validate ? new DefaultObjectModelValidator() : null);
    }

    /**
     * Load object definitions from directory.
     * <p>
     * Invalid model will be logged and ignored.
     * 
     * @param modelsDir the directory containing all the ddf file definition.
     * @param ddfValidator a validator used to validate DDF files, see {@link DefaultDDFFileValidator}. If {@code null}
     *        then there will be no validation.
     * @param modelValidator an Object model validator to ensure model is valid, see
     *        {@link DefaultObjectModelValidator}. If {@code null} then there will be no validation.
     * 
     * @since 1.1
     */
    public static List<ObjectModel> loadObjectsFromDir(File modelsDir, DDFFileValidator ddfValidator,
            ObjectModelValidator modelValidator) {
        List<ObjectModel> models = new ArrayList<>();

        // check if the folder is usable
        if (!modelsDir.isDirectory() || !modelsDir.canRead()) {
            LOG.error(MessageFormat.format(
                    "Models folder {0} is not a directory or you are not allowed to list its content",
                    modelsDir.getPath()));
        } else {
            // get all files
            for (File file : modelsDir.listFiles()) {
                if (!file.canRead())
                    continue;

                if (file.getName().endsWith(".xml")) {
                    // from DDF file
                    LOG.debug("Loading object models from DDF file {}", file.getAbsolutePath());
                    try (FileInputStream input = new FileInputStream(file)) {
                        models.addAll(loadDdfFile(input, file.getName(), ddfValidator, modelValidator));
                    } catch (IOException | InvalidModelException | InvalidDDFFileException e) {
                        LOG.warn(MessageFormat.format("Unable to load object models for {0}", file.getAbsolutePath()),
                                e);
                    }
                }
            }
        }
        return models;
    }
}
