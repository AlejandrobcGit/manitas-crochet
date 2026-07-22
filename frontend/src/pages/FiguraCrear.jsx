import { useContext, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useApiFetch } from "../api/useApiFetch";
import { FigurasContext } from "../contexts/FigurasContext";

import Header from "../components/Header";
import Footer from "../components/Footer";

import "./FiguraCrear.css";

const DIFICULTADES = ["PRINCIPIANTE", "INTERMEDIO", "AVANZADO"];

function FiguraForm() {

    const apiFetch = useApiFetch();
    const navigate = useNavigate();
    const { recargarFiguras } = useContext(FigurasContext);

    // --- Colecciones cargadas desde el backend ---
    const [categorias, setCategorias] = useState([]);
    const [coloresDisponibles, setColoresDisponibles] = useState([]);
    const [cargandoOpciones, setCargandoOpciones] = useState(true);
    const [errorOpciones, setErrorOpciones] = useState(null);

    // --- Campos de texto/número ---
    const [nombre, setNombre] = useState("");
    const [categoriaId, setCategoriaId] = useState("");
    const [dificultad, setDificultad] = useState("");
    const [descripcion, setDescripcion] = useState("");
    const [altura, setAltura] = useState("");
    const [ancho, setAncho] = useState("");
    const [autor, setAutor] = useState("");
    const [coloresIds, setColoresIds] = useState([]);
    const [comboColoresAbierto, setComboColoresAbierto] = useState(false);
    const [filtroColor, setFiltroColor] = useState("");
    const comboColoresRef = useRef(null);

    useEffect(() => {

        const cargarOpciones = async () => {

            try {

                setCargandoOpciones(true);
                setErrorOpciones(null);

                const [resCategorias, resColores] = await Promise.all([
                    apiFetch("/api/categorias"),
                    apiFetch("/api/color")
                ]);

                const [dataCategorias, dataColores] = await Promise.all([
                    resCategorias.json(),
                    resColores.json()
                ]);

                setCategorias(dataCategorias);
                setColoresDisponibles(dataColores);

            } catch (err) {

                setErrorOpciones("No se pudieron cargar categorías/colores");

            } finally {

                setCargandoOpciones(false);

            }
        };

        cargarOpciones();

    }, []);

    // Cierra el combo de colores al hacer click fuera de él
    useEffect(() => {

        const handleClickFuera = (e) => {
            if (comboColoresRef.current && !comboColoresRef.current.contains(e.target)) {
                setComboColoresAbierto(false);
            }
        };

        document.addEventListener("mousedown", handleClickFuera);

        return () => document.removeEventListener("mousedown", handleClickFuera);

    }, []);

    // Seleccionados primero (en el orden en que se eligieron), luego el resto
    // alfabéticamente; además filtra por el texto de búsqueda del combo.
    const coloresOrdenados = useMemo(() => {

        const texto = filtroColor.trim().toLowerCase();

        const filtrados = texto
            ? coloresDisponibles.filter(c => c.nombre.toLowerCase().includes(texto))
            : coloresDisponibles;

        return [...filtrados].sort((a, b) => {

            const aSel = coloresIds.includes(a.id);
            const bSel = coloresIds.includes(b.id);

            if (aSel && !bSel) return -1;
            if (!aSel && bSel) return 1;

            if (aSel && bSel) {
                return coloresIds.indexOf(a.id) - coloresIds.indexOf(b.id);
            }

            return a.nombre.localeCompare(b.nombre);

        });

    }, [coloresDisponibles, coloresIds, filtroColor]);

    const coloresSeleccionados = useMemo(
        () => coloresIds
            .map(id => coloresDisponibles.find(c => c.id === id))
            .filter(Boolean),
        [coloresIds, coloresDisponibles]
    );

    // --- Imágenes ---
    const [imagenPrincipal, setImagenPrincipal] = useState(null);
    const [imagenPrincipalPreview, setImagenPrincipalPreview] = useState(null);

    const [imagenesSecundarias, setImagenesSecundarias] = useState([]);
    const [imagenesSecundariasPreview, setImagenesSecundariasPreview] = useState([]);

    // --- Estado de envío ---
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState(null);

    const handleImagenPrincipal = (e) => {
        const file = e.target.files?.[0] || null;
        setImagenPrincipal(file);
        setImagenPrincipalPreview(file ? URL.createObjectURL(file) : null);
    };

    const handleImagenesSecundarias = (e) => {
        const files = Array.from(e.target.files || []);
        setImagenesSecundarias(files);
        setImagenesSecundariasPreview(files.map(f => URL.createObjectURL(f)));
    };

    const toggleColor = (id) => {
        setColoresIds(prev =>
            prev.includes(id)
                ? prev.filter(c => c !== id)
                : [...prev, id]
        );
    };

    const resetForm = () => {
        setNombre("");
        setCategoriaId("");
        setDificultad("");
        setDescripcion("");
        setAltura("");
        setAncho("");
        setAutor("");
        setColoresIds([]);
        setImagenPrincipal(null);
        setImagenPrincipalPreview(null);
        setImagenesSecundarias([]);
        setImagenesSecundariasPreview([]);
    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        if (!nombre.trim()) {
            setError("El nombre es obligatorio");
            return;
        }

        if (!imagenPrincipal) {
            setError("La imagen principal es obligatoria");
            return;
        }

        setError(null);
        setEnviando(true);

        try {

            // Objeto que se serializa como el "data" del @RequestPart @Valid Figura.
            // categoriaId y coloresIds son solo los IDs de Mongo; el backend
            // resuelve las referencias contra sus propias colecciones.
            const figura = {
                nombre: nombre.trim(),
                categoriaId: categoriaId || null,
                dificultad: dificultad || null,
                descripcion: descripcion.trim() || null,
                altura: altura ? Number(altura) : null,
                ancho: ancho ? Number(ancho) : null,
                autor: autor.trim() || null,
                coloresIds
            };

            const formData = new FormData();

            // Parte "data": JSON con content-type explícito, tal como espera
            // @RequestPart("data") @Valid Figura en el backend
            formData.append(
                "data",
                new Blob([JSON.stringify(figura)], { type: "application/json" })
            );

            // Parte "imagenPrincipal": obligatoria
            formData.append("imagenPrincipal", imagenPrincipal);

            // Parte "imagenesSecundarias": opcional, se puede repetir la key
            imagenesSecundarias.forEach(file => {
                formData.append("imagenesSecundarias", file);
            });

            // No fijar Content-Type a mano: el navegador añade el boundary
            // correcto del multipart automáticamente.
            const response = await apiFetch("/api/figuras", {
                method: "POST",
                body: formData
            });

            if (!response.ok) {
                throw new Error("Error al crear la figura");
            }

            const nuevaFigura = await response.json();

            await recargarFiguras?.();

            resetForm();

            navigate(`/figuras/${nuevaFigura.id}`);

        } catch (err) {

            setError(err.message || "Error al crear la figura");

        } finally {

            setEnviando(false);

        }
    };

    return (

        <div className="app">

            <Header />

            <main className="form-main">

                <h1 className="form-title">Nueva figura</h1>

                <form className="figura-form" onSubmit={handleSubmit}>

                    {error && (
                        <p className="form-status form-status--error">
                            {error}
                        </p>
                    )}

                    <div className="form-grid">

                        <label className="form-field">
                            <span>Nombre *</span>
                            <input
                                type="text"
                                value={nombre}
                                onChange={e => setNombre(e.target.value)}
                                required
                            />
                        </label>

                        <label className="form-field">
                            <span>Categoría</span>
                            <select
                                value={categoriaId}
                                onChange={e => setCategoriaId(e.target.value)}
                                disabled={cargandoOpciones}
                            >
                                <option value="">Selecciona…</option>
                                {categorias.map(cat => (
                                    <option key={cat.id} value={cat.id}>
                                        {cat.nombre}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label className="form-field">
                            <span>Dificultad</span>
                            <select
                                value={dificultad}
                                onChange={e => setDificultad(e.target.value)}
                            >
                                <option value="">Selecciona…</option>
                                {DIFICULTADES.map(d => (
                                    <option key={d} value={d}>{d}</option>
                                ))}
                            </select>
                        </label>

                        <label className="form-field">
                            <span>Autor</span>
                            <input
                                type="text"
                                value={autor}
                                onChange={e => setAutor(e.target.value)}
                            />
                        </label>

                        <label className="form-field">
                            <span>Alto (cm)</span>
                            <input
                                type="number"
                                min="0"
                                step="0.1"
                                value={altura}
                                onChange={e => setAltura(e.target.value)}
                            />
                        </label>

                        <label className="form-field">
                            <span>Ancho (cm)</span>
                            <input
                                type="number"
                                min="0"
                                step="0.1"
                                value={ancho}
                                onChange={e => setAncho(e.target.value)}
                            />
                        </label>

                    </div>

                    <label className="form-field">
                        <span>Descripción</span>
                        <textarea
                            rows={4}
                            value={descripcion}
                            onChange={e => setDescripcion(e.target.value)}
                        />
                    </label>

                    <div className="form-field" ref={comboColoresRef}>

                        <span>Colores</span>

                        {errorOpciones && (
                            <p className="form-status form-status--error">
                                {errorOpciones}
                            </p>
                        )}

                        <div className="form-combo">

                            <button
                                type="button"
                                className="form-combo__control"
                                onClick={() => setComboColoresAbierto(prev => !prev)}
                                disabled={cargandoOpciones}
                            >

                                {coloresSeleccionados.length === 0 ? (
                                    <span className="form-combo__placeholder">
                                        {cargandoOpciones ? "Cargando colores…" : "Selecciona colores…"}
                                    </span>
                                ) : (
                                    <span className="form-combo__tags">
                                        {coloresSeleccionados.map(color => (
                                            <span className="form-combo__tag" key={color.id}>
                                                <span
                                                    className="form-combo__tag-swatch"
                                                    style={{ backgroundColor: color.codigo }}
                                                />
                                                {color.nombre}
                                                <span
                                                    role="button"
                                                    tabIndex={-1}
                                                    className="form-combo__tag-quitar"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        toggleColor(color.id);
                                                    }}
                                                >
                                                    ×
                                                </span>
                                            </span>
                                        ))}
                                    </span>
                                )}

                                <span className="form-combo__flecha">
                                    {comboColoresAbierto ? "▲" : "▼"}
                                </span>

                            </button>

                            {comboColoresAbierto && (

                                <div className="form-combo__panel">

                                    <input
                                        type="text"
                                        className="form-combo__buscador"
                                        placeholder="Buscar color…"
                                        value={filtroColor}
                                        onChange={e => setFiltroColor(e.target.value)}
                                        autoFocus
                                    />

                                    <div className="form-combo__lista">

                                        {coloresOrdenados.map(color => (

                                            <label
                                                className={
                                                    "form-combo__opcion" +
                                                    (coloresIds.includes(color.id) ? " form-combo__opcion--activa" : "")
                                                }
                                                key={color.id}
                                            >

                                                <input
                                                    type="checkbox"
                                                    checked={coloresIds.includes(color.id)}
                                                    onChange={() => toggleColor(color.id)}
                                                />

                                                <span
                                                    className="form-combo__opcion-swatch"
                                                    style={{ backgroundColor: color.codigo }}
                                                />

                                                {color.nombre}

                                            </label>

                                        ))}

                                        {coloresOrdenados.length === 0 && (
                                            <p className="form-combo__vacio">
                                                Sin resultados.
                                            </p>
                                        )}

                                    </div>

                                </div>

                            )}

                        </div>

                    </div>

                    <label className="form-field">
                        <span>Imagen principal *</span>
                        <input
                            type="file"
                            accept="image/*"
                            onChange={handleImagenPrincipal}
                            required
                        />
                    </label>

                    {imagenPrincipalPreview && (
                        <img
                            className="form-preview form-preview--principal"
                            src={imagenPrincipalPreview}
                            alt="Vista previa imagen principal"
                        />
                    )}

                    <label className="form-field">
                        <span>Imágenes secundarias</span>
                        <input
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={handleImagenesSecundarias}
                        />
                    </label>

                    {imagenesSecundariasPreview.length > 0 && (
                        <div className="form-preview-grid">
                            {imagenesSecundariasPreview.map((src, i) => (
                                <img
                                    key={i}
                                    className="form-preview"
                                    src={src}
                                    alt={`Vista previa secundaria ${i + 1}`}
                                />
                            ))}
                        </div>
                    )}

                    <button
                        type="submit"
                        className="form-btn form-btn--principal"
                        disabled={enviando}
                    >
                        {enviando ? "Creando…" : "Crear figura"}
                    </button>

                </form>

            </main>

            <Footer />

        </div>

    );
}

export default FiguraForm;