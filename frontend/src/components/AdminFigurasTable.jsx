import "./AdminFigurasTable.css";

const API_URL = "http://localhost:8080";

function AdminFigurasTable({
    figuras,
    onEditar,
    onEliminar
}) {

    return (
        <div className="admin-tabla-container">

            <table className="admin-tabla">

                <thead>
                    <tr>
                        <th>Imagen</th>
                        <th>Nombre</th>
                        <th>Categoria</th>
                        <th>Acciones</th>
                    </tr>
                </thead>

                <tbody>

                    {figuras.map(figura => {

                        const imageUrl = figura.imagenPrincipal
                            ? `${API_URL}/api/imagenes/${figura.imagenPrincipal}`
                            : null;

                        return (
                            <tr key={figura.id}>
                                <td>

                                    <img
                                        src={imageUrl}
                                        alt={figura.nombre}
                                        className="admin-tabla__imagen"
                                    />

                                </td>
                                <td>{figura.nombre}</td>
                                <td>{figura.categoria}</td>
                                <td>
                                    <div className="admin-tabla__acciones">

                                        <button className="btn-editar">
                                            Editar
                                        </button>

                                        <button className="btn-eliminar">
                                            Eliminar
                                        </button>

                                    </div>
                                </td>

                            </tr>
                        );

                    })}
                </tbody>
            </table>
        </div>
    );
}

export default AdminFigurasTable;